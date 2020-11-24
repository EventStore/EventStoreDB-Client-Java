package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class SubscribeToAllTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Test
    public void testAllSubscriptionDeliversAllowsCancellationDuringStream() throws InterruptedException, ExecutionException {
        Streams streams = server.getStreamsAPI();

        final CountDownLatch receivedEvents = new CountDownLatch(1000);
        final CountDownLatch cancellation = new CountDownLatch(1);

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                receivedEvents.countDown();
            }

            @Override
            public void onCancelled(Subscription subscription) {
                cancellation.countDown();
            }

            @Override
            public void onError(Subscription subscription, Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        Subscription result = streams.subscribeToAll(listener)
                .fromStart()
                .execute()
                .get();

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }

    @Test
    public void testAllSubscriptionWithFilterDeliversCorrectEvents() throws InterruptedException, ExecutionException {
        final TestPosition[] expectedPositions = TestDataLoader.loadSerializedPositions(
                "all-positions-filtered-stream194-e0-e30");
        final long[] expectedStreamVersions = TestDataLoader.loadSerializedStreamVersions(
                "all-versions-filtered-stream194-e0-e30");

        assertEquals(expectedPositions.length, expectedStreamVersions.length);

        final CountDownLatch receivedEvents = new CountDownLatch(expectedStreamVersions.length);
        final CountDownLatch cancellation = new CountDownLatch(1);

        SubscriptionListener listener = new SubscriptionListener() {
            int current = 0;

            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                RecordedEvent record = event.getEvent();

                assertEquals(new StreamRevision(expectedStreamVersions[current]), record.getStreamRevision());
                expectedPositions[current].assertEquals(record.getPosition());

                receivedEvents.countDown();
                current++;
            }

            @Override
            public void onCancelled(Subscription subscription) {
                cancellation.countDown();
            }

            @Override
            public void onError(Subscription subscription, Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withEventTypePrefix("eventType-194")
                .build();

        Subscription result = server.getClient().streams()
                .subscribeToAll(listener)
                .fromStart()
                .filter(filter)
                .execute()
                .get();

        assertNotNull(result.getSubscriptionId());
        assertNotEquals("", result.getSubscriptionId());

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }
}


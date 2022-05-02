package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class SubscribeToAllTests extends ESDBTests {
    @Test
    public void testAllSubscriptionDeliversAllowsCancellationDuringStream() throws InterruptedException, ExecutionException {
        EventStoreDBClient streams = getPopulatedServer().getClient();

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

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .fromStart();

        Subscription result = streams.subscribeToAll(listener, options)
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

                assertEquals(expectedStreamVersions[current], record.getRevision());
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
                .addEventTypePrefix("eventType-194")
                .build();

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .fromStart()
                .filter(filter);

        Subscription result = getPopulatedServer().getClient()
                .subscribeToAll(listener, options)
                .get();

        assertNotNull(result.getSubscriptionId());
        assertNotEquals("", result.getSubscriptionId());

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }
}


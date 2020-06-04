package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class SubscribeToAllTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testAllSubscriptionDeliversAllowsCancellationDuringStream() throws InterruptedException, ExecutionException {
        final CountDownLatch receivedEvents = new CountDownLatch(1000);
        final CountDownLatch cancellation = new CountDownLatch(1);

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            void onEvent(Subscription subscription, ResolvedEvent event) {
                receivedEvents.countDown();
            }

            @Override
            void onCancelled(Subscription subscription) {
                cancellation.countDown();
            }

            @Override
            void onError(Subscription subscription, Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        CompletableFuture<Subscription> future = client.instance.subscribeToAll(Position.START, false, listener);
        Subscription result = future.get();

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }

    @Test
    public void testAllSubscriptionWithFilterDeliversCorrectEvents() throws InterruptedException, ExecutionException {
        final TestPosition[] expectedPositions = TestDataLoader.loadSerializedPositions(
                "all-positions-filtered-stream194-e0-e30");
        final long[] expectedStreamVersion = TestDataLoader.loadSerializedStreamVersions(
                "all-versions-filtered-stream194-e0-e30");

        final long[] expectedStreamVersions = new long[]{
                194, 394, 594, 794, 994, 1194, 1394, 1594, 1794, 1994, 140, 141, 142, 143, 144,
                145, 146, 147, 148, 149, 340, 341, 342, 343, 344, 345, 346, 347, 348, 349
        };
        assertEquals(expectedPositions.length, expectedStreamVersions.length);

        final CountDownLatch receivedEvents = new CountDownLatch(expectedStreamVersions.length);
        final CountDownLatch cancellation = new CountDownLatch(1);

        SubscriptionListener listener = new SubscriptionListener() {
            int current = 0;

            @Override
            void onEvent(Subscription subscription, ResolvedEvent event) {
                RecordedEvent record = event.getEvent();

                assertEquals(new StreamRevision(expectedStreamVersions[current]), record.getStreamRevision());
                expectedPositions[current].assertEquals(record.getPosition());

                receivedEvents.countDown();
                current++;
            }

            @Override
            void onCancelled(Subscription subscription) {
                cancellation.countDown();
            }

            @Override
            void onError(Subscription subscription, Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withEventTypePrefix("eventType-194")
                .build();

        CompletableFuture<Subscription> future = client.instance.subscribeToAll(Position.START, false, listener, filter);
        Subscription result = future.get();

        assertNotNull(result.getSubscriptionId());
        assertNotEquals("", result.getSubscriptionId());

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }
}


package com.eventstore.dbclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class SubscribeToAllTests extends ESDBTests {

    @Test
    public void testStreamSubscriptionIsAbortedByShutdownOfClient() throws InterruptedException, ExecutionException {
        EventStoreDBClient streams = EventStoreDBClient.create(getPopulatedServer().getSettings());

        final CountDownLatch aborted = new CountDownLatch(1);

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
            }

            @Override
            public void onCancelled(Subscription subscription) {
            }

            @Override
            public void onError(Subscription subscription, Throwable throwable) {
                if (throwable instanceof StatusRuntimeException) {
                    StatusRuntimeException statusRuntimeException = (StatusRuntimeException) throwable;
                    if (statusRuntimeException.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                        aborted.countDown();
                    }
                }
            }
        };

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .fromStart();

        streams.subscribeToAll(listener, options)
                .get();
        streams.shutdown();

        aborted.await();
    }


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


package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class SubscribeToStreamTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testStreamSubscriptionDeliversAllowsCancellationDuringStream() throws InterruptedException, ExecutionException {
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

        CompletableFuture<Subscription> future = client.instance
                .subscribeToStream("dataset20M-0", StreamRevision.START, false, listener);
        Subscription result = future.get();

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }

    @Test
    public void testStreamSubscriptionDeliversAllEventsInStream() throws InterruptedException, ExecutionException {
        final CountDownLatch receivedEvents = new CountDownLatch(6000);
        final CountDownLatch cancellation = new CountDownLatch(1);

        class CountingListener extends SubscriptionListener {
            public int current = 0;

            @Override
            void onEvent(Subscription subscription, ResolvedEvent event) {
                assertEquals(new StreamRevision(current), event.getEvent().getStreamRevision());
                current += 1;
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
        }

        CountingListener listener = new CountingListener();
        CompletableFuture<Subscription> future = client.instance
                .subscribeToStream("dataset20M-0", StreamRevision.START, false, listener);
        Subscription result = future.get();

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }

    @Test
    public void testStreamSubscriptionDeliversAllEventsInStreamAndListensForNewEvents() throws Throwable {
        final CountDownLatch receivedEvents = new CountDownLatch(6000);
        final CountDownLatch appendedEvents = new CountDownLatch(1);
        final CountDownLatch cancellation = new CountDownLatch(1);

        // Appended event data
        final String testStreamName = "dataset20M-0";
        final String eventType = "TestEvent";
        final String eventId = "84c8e36c-4e64-11ea-8b59-b7f658acfc9f";
        final byte[] eventMetaData = new byte[]{0xd, 0xe, 0xa, 0xd};
        final byte[] eventData = new byte[]{0xb, 0xe, 0xe, 0xf};

        class CountingListener extends SubscriptionListener {
            public int current = 0;

            @Override
            void onEvent(Subscription subscription, ResolvedEvent event) {
                assertEquals(new StreamRevision(current), event.getEvent().getStreamRevision());
                current += 1;

                if (current <= 6000) {
                    receivedEvents.countDown();
                } else {
                    appendedEvents.countDown();

                    // Assert the event we appended has correct values
                    RecordedEvent e = event.getEvent();
                    assertEquals(eventId, e.getEventId().toString());
                    assertEquals(new StreamRevision(6000), e.getStreamRevision());
                    assertEquals(testStreamName, e.getStreamId());
                    assertEquals(eventType, e.getEventType());
                    assertArrayEquals(eventMetaData, e.getUserMetadata());
                    assertArrayEquals(eventData, e.getEventData());
                }
            }

            @Override
            void onCancelled(Subscription subscription) {
                cancellation.countDown();
            }

            @Override
            void onError(Subscription subscription, Throwable throwable) {
                fail(throwable.getMessage());
            }
        }

        // Listen to everything already in the stream
        CountingListener listener = new CountingListener();
        CompletableFuture<Subscription> subscriptionFuture = client.instance.
                subscribeToStream(testStreamName, StreamRevision.START, false, listener);
        Subscription subscription = subscriptionFuture.get();
        receivedEvents.await();

        // Write a new event
        ArrayList<ProposedEvent> events = new ArrayList<>();
        events.add(new ProposedEvent(UUID.fromString(eventId), eventType,
                "application/octet-stream", eventData, eventMetaData));

        CompletableFuture<WriteResult> writeFuture = client.instance.appendToStream(testStreamName,
                new StreamRevision(5999), events);
        WriteResult writeResult = writeFuture.get();

        assertEquals(new StreamRevision(6000), writeResult.getNextExpectedRevision());

        // Assert the event was forwarded to the subscription
        appendedEvents.await();

        // Clean up subscription
        subscription.stop();
        cancellation.await();
    }
}

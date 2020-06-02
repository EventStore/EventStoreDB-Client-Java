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
        final Position[] expectedPositions = new Position[]{
                new Position(137783, 108819),
                new Position(167653, 138689),
                new Position(197523, 168559),
                new Position(227393, 198429),
                new Position(257263, 228299),
                new Position(287133, 258169),
                new Position(317003, 288039),
                new Position(346873, 317909),
                new Position(376743, 347779),
                new Position(406613, 377649),
                new Position(12932074, 12903110),
                new Position(12961944, 12932980),
                new Position(12991814, 12962850),
                new Position(13021684, 12992720),
                new Position(13051554, 13022590),
                new Position(13081424, 13052460),
                new Position(13111294, 13082330),
                new Position(13141164, 13112200),
                new Position(13171034, 13142070),
                new Position(13200904, 13171940),
                new Position(17932093, 17903129),
                new Position(17961963, 17932999),
                new Position(17991833, 17962869),
                new Position(18021703, 17992739),
                new Position(18051573, 18022609),
                new Position(18081443, 18052479),
                new Position(18111313, 18082349),
                new Position(18141183, 18112219),
                new Position(18171053, 18142089),
                new Position(18200923, 18171959),
        };
        final long[] expectedStreamVersions = new long[]{
                194, 394, 594, 794, 994, 1194, 1394, 1594, 1794, 1994, 2194, 2394, 2594, 2794, 2994, 3194, 3394, 3594,
                3794, 3994, 4194, 4394, 4594, 4794, 4994, 5194, 5394, 5594, 5794, 5994,
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
                assertEquals(expectedPositions[current], record.getPosition());

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
                .withEventTypeRegularExpression("^eventType-194$")
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


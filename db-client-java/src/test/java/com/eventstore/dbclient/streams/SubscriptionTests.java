package com.eventstore.dbclient.streams;

import com.eventstore.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public interface SubscriptionTests extends ConnectionAware {
    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testSubscription() throws Throwable {
        final String streamName = generateName();
        final ArrayList<BazEvent> before = generateBazEvent(3);
        final ArrayList<BazEvent> after = generateBazEvent(3);
        final ArrayList<BazEvent> expected = new ArrayList<>();

        expected.addAll(before);
        expected.addAll(after);

        getDefaultClient().appendToStream(streamName, serializeBazEvents(before).iterator()).get();
        final CountDownLatch receivedEvents = new CountDownLatch(before.size());
        final CountDownLatch appendedEvents = new CountDownLatch(after.size());
        final AtomicInteger count = new AtomicInteger(0);

        getLogger().debug("Before subscribing to stream");
        Subscription subscription = getDefaultClient().subscribeToStream(streamName, new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                try {
                    BazEvent source = expected.get(count.getAndIncrement());
                    BazEvent actual = deserializeBazEvent(event.getOriginalEvent().getEventData());

                    Assertions.assertEquals(source.getAge(), actual.getAge());
                    Assertions.assertEquals(source.getName(), actual.getName());
                } catch (IOException e) {
                    getLogger().error("Error when handling subscription", e);
                    Assertions.fail(e);
                }

                if (count.get() <= before.size()) {
                    receivedEvents.countDown();
                } else {
                    appendedEvents.countDown();
                }
            }

            @Override
            public void onCancelled(Subscription subscription, Throwable throwable) {
                if (throwable == null)
                    return;

                getLogger().error("Error when running subscription", throwable);
                Assertions.fail(throwable);
            }
        }).get();
        getLogger().debug("After subscribing to stream");

        getLogger().debug("Waiting for receiving events to come along");
        receivedEvents.await();
        getLogger().debug("Done");

        getDefaultClient().appendToStream(streamName, serializeBazEvents(after).iterator()).get();

        getLogger().debug("Waiting for appending events to come along");
        appendedEvents.await();
        getLogger().debug("Done");
        Assertions.assertEquals(expected.size(), count.get());
        getLogger().debug("Before stopping subscription");
        subscription.stop();
        getLogger().debug("Done");
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testSubscriptionToAllWithFilter() throws Throwable {
        String eventType = generateName();
        final ArrayList<BazEvent> before = generateBazEvent(3);
        final ArrayList<BazEvent> after = generateBazEvent(3);
        final ArrayList<BazEvent> expected = new ArrayList<>();

        expected.addAll(before);
        expected.addAll(after);

        getDefaultClient().appendToStream(generateName(), serializeBazEvents(before, eventType).iterator()).get();
        final CountDownLatch receivedEvents = new CountDownLatch(before.size());
        final CountDownLatch appendedEvents = new CountDownLatch(after.size());
        final AtomicInteger count = new AtomicInteger(0);
        Subscription subscription = getDefaultClient().subscribeToAll(new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                try {
                    BazEvent source = expected.get(count.getAndIncrement());
                    BazEvent actual = deserializeBazEvent(event.getOriginalEvent().getEventData());

                    Assertions.assertEquals(source.getAge(), actual.getAge());
                    Assertions.assertEquals(source.getName(), actual.getName());
                } catch (IOException e) {
                    getLogger().error("Error when handling subscription", e);
                    throw new RuntimeException(e);
                }

                if (count.get() <= before.size()) {
                    receivedEvents.countDown();
                } else {
                    appendedEvents.countDown();
                }
            }

            @Override
            public void onCancelled(Subscription subscription, Throwable throwable) {
                if (throwable == null)
                    return;

                Assertions.fail(throwable);
            }
        }, SubscribeToAllOptions.get()
                .filter(SubscriptionFilter
                        .newBuilder()
                        .addEventTypePrefix(eventType)
                        .build())).get();

        receivedEvents.await();

        getDefaultClient().appendToStream(generateName(), serializeBazEvents(after, eventType).iterator()).get();

        appendedEvents.await();
        Assertions.assertEquals(expected.size(), count.get());
        subscription.stop();
    }
}

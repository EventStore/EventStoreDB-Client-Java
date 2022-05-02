package com.eventstore.dbclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class PersistentSubscriptionToAllWithFilterTests extends ESDBTests {
    @Test
    public void testPersistentSubscriptionToAllWithFilter() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        EventStoreDBClient streamsClient = getEmptyServer().getClient();
        String groupName = generateName();
        int filteredEventTypeCount = 10;
        String filteredEventType = "filtered-event-type";
        String otherEventType = UUID.randomUUID().toString();

        streamsClient.appendToStream(generateName(), generateEvents(5, otherEventType).iterator()).get();
        streamsClient.appendToStream(generateName(), generateEvents(filteredEventTypeCount, filteredEventType).iterator()).get();
        streamsClient.appendToStream(generateName(), generateEvents(10, otherEventType).iterator()).get();

        final CountDownLatch receivedEvents = new CountDownLatch(filteredEventTypeCount);
        final CountDownLatch cancellation = new CountDownLatch(1);

        PersistentSubscriptionListener listener = new PersistentSubscriptionListener() {
            int current = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                RecordedEvent record = event.getEvent();

                Assertions.assertEquals(filteredEventType, record.getEventType());

                receivedEvents.countDown();
                current++;
            }

            @Override
            public void onCancelled(PersistentSubscription subscription) {
                cancellation.countDown();
            }

            @Override
            public void onError(PersistentSubscription subscription, Throwable throwable) {
                Assertions.fail(throwable.getMessage());
            }
        };

        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
            .addEventTypePrefix(filteredEventType)
            .build();

        try {
            client.createToAll(groupName, CreatePersistentSubscriptionToAllOptions.get()
                            .fromStart()
                            .filter(filter))
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeatureException && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }

            return;
        }

        PersistentSubscription result = client.subscribeToAll(groupName, listener)
            .get();

        Assertions.assertNotNull(result.getSubscriptionId());
        Assertions.assertNotEquals("", result.getSubscriptionId());

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }
}

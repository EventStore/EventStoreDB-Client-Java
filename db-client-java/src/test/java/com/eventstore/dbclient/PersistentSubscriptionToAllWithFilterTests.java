package com.eventstore.dbclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class PersistentSubscriptionToAllWithFilterTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(true);
    private EventStoreDBPersistentSubscriptionsClient client;
    private EventStoreDBClient dbClient;

    @Before
    public void before() throws Throwable {
        client = server.getPersistentSubscriptionsClient();
        dbClient = server.getClient();
    }

    @Test
    public void testPersistentSubscriptionToAllWithFilter() throws Throwable {
        int filteredEventTypeCount = 10;
        String filteredEventType = "filtered-event-type";
        String otherEventType = UUID.randomUUID().toString();

        AddEvents("some-stream-a", GenerateEvents(5, otherEventType));
        AddEvents("some-stream-b", GenerateEvents(filteredEventTypeCount, filteredEventType));
        AddEvents("some-stream-c", GenerateEvents(10, otherEventType));

        final CountDownLatch receivedEvents = new CountDownLatch(filteredEventTypeCount);
        final CountDownLatch cancellation = new CountDownLatch(1);

        PersistentSubscriptionListener listener = new PersistentSubscriptionListener() {
            int current = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
                RecordedEvent record = event.getEvent();

                assertEquals(filteredEventType, record.getEventType());

                receivedEvents.countDown();
                current++;
            }

            @Override
            public void onCancelled(PersistentSubscription subscription) {
                cancellation.countDown();
            }

            @Override
            public void onError(PersistentSubscription subscription, Throwable throwable) {
                fail(throwable.getMessage());
            }
        };

        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
            .withEventTypePrefix(filteredEventType)
            .build();

        client.createToAll("filtered-group", PersistentSubscriptionToAllSettings.builder()
            .fromStart()
            .filter(filter)
            .build())
            .get();

        PersistentSubscription result = client.subscribeToAll("filtered-group", listener)
            .get();

        assertNotNull(result.getSubscriptionId());
        assertNotEquals("", result.getSubscriptionId());

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }

    @After
    public void after() {
        if(client == null)
            return;

        try {
            client.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<EventData> GenerateEvents(int amount, String eventType) {
        ArrayList<EventData> events = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            events.add(EventDataBuilder.json(UUID.randomUUID(), eventType, new byte[]{}).build());
        }

        return events;
    }

    private void AddEvents(String stream, ArrayList<EventData> events) {
        dbClient.appendToStream(stream, events.iterator());
    }
}

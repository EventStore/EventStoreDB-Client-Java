package com.eventstore.dbclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersistentSubscriptionManagementTests extends ESDBTests {
    @Test
    @Order(1)
    public void testListPersistentSubscriptions() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String groupName = generateName();
        String streamA = generateName();
        String streamB = generateName();

        client.create(streamA, groupName)
                .get();

        client.create(streamB, groupName)
                .get();

        List<PersistentSubscriptionInfo> subs = client.listAll().get();

        int count = 0;
        for (PersistentSubscriptionInfo info: subs) {
            if (info.getEventStreamId().equals(streamA) || info.getEventStreamId().equals(streamB)) {
                count++;
            }
        }

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(2)
    public void testListPersistentSubscriptionsForStream() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String streamName = generateName();
        String groupName = generateName();

        client.create(streamName, groupName)
                .get();

        for (int i = 0; i < 10; i++) {
            List<PersistentSubscriptionInfo> subs;

            try {
                subs = client.listForStream(streamName).get();
            } catch (ResourceNotFoundException e) {
                Thread.sleep(500);
                continue;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof StatusRuntimeException) {
                    StatusRuntimeException status = (StatusRuntimeException) e.getCause();
                    if (status.getStatus().getCode() == Status.Code.INTERNAL) {
                        // Some weird use-case when running in Github Action, it's usually a retryable error from the gRPC
                        // implementation: "Abrupt GOAWAY closed sent stream. HTTP/2 error code: STREAM_CLOSED"
                        Thread.sleep(500);
                        continue;
                    }
                }

                throw e;
            }

            Assertions.assertEquals(subs.size(), 1);
            Assertions.assertEquals(subs.get(0).getEventStreamId(), streamName);
            break;
        }
    }

    @Test
    @Order(3)
    public void testListPersistentSubscriptionsToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String groupName = generateName();
        try {
            client.createToAll(groupName)
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeature && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }

            return;
        }

        List<PersistentSubscriptionInfo> subs = client.listToAll().get();

        Assertions.assertTrue(subs.size() > 1);

        boolean found = false;

        for (PersistentSubscriptionInfo info : subs) {
            if (info.getEventStreamId().equals("$all") && info.getGroupName().equals(groupName)) {
                found = true;
                break;
            }
        }

        Assertions.assertTrue(found);
    }

    @Test
    @Order(4)
    public void testGetPersistentSubscriptionInfo() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String streamName = generateName();
        String groupName = generateName();
        client.create(streamName, groupName)
                .get();
        Optional<PersistentSubscriptionInfo> result = Optional.empty();
        for (int i = 0; i < 10; i++) {
            result = client.getInfo(streamName, groupName).get();
            if (!result.isPresent()) {
                Thread.sleep(1000);
                continue;
            }

            break;
        }

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(streamName, result.get().getEventStreamId());
    }

    @Test
    @Order(5)
    public void testGetPersistentSubscriptionInfoToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String groupName = generateName();
        try {
            client.createToAll(groupName)
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeature && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }

            return;
        }

        Optional<PersistentSubscriptionInfo> result = client.getInfoToAll(groupName).get();

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("$all", result.get().getEventStreamId());
        Assertions.assertEquals(result.get().getGroupName(), groupName);
    }

    @Test
    @Order(6)
    public void testGetPersistentSubscriptionInfoNotExisting() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        Optional<PersistentSubscriptionInfo> result = client.getInfo(generateName(), generateName()).get();

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(7)
    public void testReplayParkedMessages() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        final EventStoreDBClient streamClient = getEmptyServer().getClient();
        final String streamName = generateName();
        final String groupName = generateName();
        client.create(streamName, groupName)
                .get();

        final CompletableFuture allNacked = new CompletableFuture();
        final CompletableFuture allReplayed = new CompletableFuture();

        client.subscribe(streamName, groupName, new PersistentSubscriptionListener() {
            int count = 0;
            @Override
            public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
                if (count < 2)
                    subscription.nack(NackAction.Park, "because reason", event);
                else
                    subscription.ack(event);

                count++;

                if (count == 2) {
                    allNacked.complete(null);
                }

                if (count >= 4) {
                    allReplayed.complete(null);
                    subscription.stop();
                }
            }
        }).get();

        for (int i = 0; i < 2; i++) {
            EventData data = EventData.builderAsJson("foobar", new Foo()).build();
            streamClient.appendToStream(streamName, data).get();
        }

        CompletableFuture.runAsync(() -> {
            try {
                allNacked.get(10, TimeUnit.SECONDS);
                // We give the server some time to park those events.
                Thread.sleep(10000);
                client.replayParkedMessages(streamName, groupName).get();
                allReplayed.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).get(40, TimeUnit.SECONDS);
    }

    @Test
    @Order(8)
    public void testReplayParkedMessagesToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String streamName = generateName();
        String groupName = generateName();
        final EventStoreDBClient streamClient = getEmptyServer().getClient();

        try {
            client.createToAll(groupName)
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeature && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }

            return;
        }

        final CompletableFuture allNacked = new CompletableFuture();
        final CompletableFuture allReplayed = new CompletableFuture();

        client.subscribeToAll(groupName, new PersistentSubscriptionListener() {
            int count = 0;
            @Override
            public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
                if (count < 2 && event.getOriginalEvent().getStreamId().equals(streamName))
                    subscription.nack(NackAction.Park, "because reason", event);
                else
                    subscription.ack(event);

                if (event.getOriginalEvent().getStreamId().equals(streamName) || event.getEvent().getStreamId().equals(streamName))
                count++;

                if (count == 2) {
                    allNacked.complete(null);
                }

                if (count >= 4) {
                    allReplayed.complete(null);
                    subscription.stop();
                }
            }
        }).get();

        for (int i = 0; i < 2; i++) {
            EventData data = EventData.builderAsJson("foobar", new Foo()).build();
            streamClient.appendToStream(streamName, data).get();
        }

        CompletableFuture.runAsync(() -> {
            try {
                allNacked.get(10, TimeUnit.SECONDS);
                // We give the server some time to park those events.
                Thread.sleep(10000);
                client.replayParkedMessagesToAll(groupName).get();
                allReplayed.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).get(40, TimeUnit.SECONDS);
    }

    @Test
    @Order(9)
    public void testEncoding() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String streamName = String.format("/foo/%s/stream", generateName());
        String groupName = String.format("/foo/%s/group", generateName());

        client.create(streamName, groupName).get();
        Optional<PersistentSubscriptionInfo> info = Optional.empty();
        for (int i = 0; i < 10; i++) {
            info = client.getInfo(streamName, groupName).get();

            if (!info.isPresent()) {
                Thread.sleep(500);
                continue;
            }

            break;
        }

       Assertions.assertTrue(info.isPresent());
       Assertions.assertEquals(info.get().getEventStreamId(), streamName);
       Assertions.assertEquals(info.get().getGroupName(), groupName);
    }

    @Test
    @Order(10)
    public void testRestartSubsystem() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        client.restartSubsystem().get();
    }
}

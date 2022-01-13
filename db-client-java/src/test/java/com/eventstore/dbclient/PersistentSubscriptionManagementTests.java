package com.eventstore.dbclient;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PersistentSubscriptionManagementTests extends PersistentSubscriptionTestsBase {
    @Test
    public void testListPersistentSubscriptions() throws Throwable {
        client.create("stream-a", "group")
                .get();

        client.create("stream-b", "group")
                .get();

        List<PersistentSubscriptionInfo> subs = client.listAll().get();

        int count = 0;
        for (PersistentSubscriptionInfo info: subs) {
            if (info.getEventStreamId().equals("stream-a") || info.getEventStreamId().equals("stream-b")) {
                count++;
            }
        }

        Assert.assertEquals(2, count);
    }

    @Test
    public void testListPersistentSubscriptionsForStream() throws Throwable {
        client.create("stream-c", "group")
                .get();

        List<PersistentSubscriptionInfo> subs = client.listForStream("stream-c").get();

        Assert.assertEquals(subs.size(), 1);
        Assert.assertEquals(subs.get(0).getEventStreamId(), "stream-c");
    }

    @Test
    public void testListPersistentSubscriptionsToAll() throws Throwable {
        client.createToAll("group")
                .get();

        List<PersistentSubscriptionInfo> subs = client.listToAll().get();

        Assert.assertEquals(subs.size(), 1);

        Assert.assertEquals(subs.get(0).getEventStreamId(), "$all");
        Assert.assertEquals(subs.get(0).getGroupName(), "group");
    }

    @Test
    public void testGetPersistentSubscriptionInfo() throws Throwable {
        client.create("stream-d", "group")
                .get();

        Optional<PersistentSubscriptionInfo> result = client.getInfo("stream-d", "group").get();

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("stream-d", result.get().getEventStreamId());
    }

    @Test
    public void testGetPersistentSubscriptionInfoToAll() throws Throwable {
        client.createToAll("group")
                .get();

        Optional<PersistentSubscriptionInfo> result = client.getInfoToAll("group").get();

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("$all", result.get().getEventStreamId());
        Assert.assertEquals(result.get().getGroupName(), "group");
    }

    @Test
    public void testGetPersistentSubscriptionInfoNotExisting() throws Throwable {
        Optional<PersistentSubscriptionInfo> result = client.getInfo("not_existing", "group").get();

        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testReplayParkedMessages() throws Throwable {
        client.create("stream-e", "group")
                .get();

        EventData data = EventData.builderAsJson("foobar", new Foo()).build();
        streamClient.appendToStream("stream-e", data).get();
        data = EventData.builderAsJson("foobar", new Foo()).build();
        streamClient.appendToStream("stream-e", data).get();

        final CompletableFuture allNacked = new CompletableFuture();
        final CompletableFuture allReplayed = new CompletableFuture();

        client.subscribe("stream-e", "group", new PersistentSubscriptionListener() {
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

        allNacked.get(10, TimeUnit.SECONDS);
        client.replayParkedMessages("stream-e", "group").get();
        allReplayed.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void testReplayParkedMessagesToAll() throws Throwable {
        client.createToAll("group")
                .get();

        EventData data = EventData.builderAsJson("foobar", new Foo()).build();
        String streamName = "stream-e";
        streamClient.appendToStream(streamName, data).get();
        data = EventData.builderAsJson("foobar", new Foo()).build();
        streamClient.appendToStream(streamName, data).get();

        final CompletableFuture allNacked = new CompletableFuture();
        final CompletableFuture allReplayed = new CompletableFuture();

        client.subscribeToAll("group", new PersistentSubscriptionListener() {
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

        allNacked.get(10, TimeUnit.SECONDS);
        client.replayParkedMessagesToAll( "group").get();
        allReplayed.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void testEncoding() throws Throwable {
        String streamName = "/foo/bar/stream";
        String groupName = "/foo/bar/group";

        client.create(streamName, groupName).get();
        Optional<PersistentSubscriptionInfo> info = client.getInfo(streamName, groupName).get();

       Assert.assertTrue(info.isPresent());
       Assert.assertEquals(info.get().getEventStreamId(), streamName);
       Assert.assertEquals(info.get().getGroupName(), groupName);
    }

    @Test
    public void testRestartSubsystem() throws Throwable {
        client.restartSubsystem().get();
    }
}

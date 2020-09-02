package com.eventstore.dbclient;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ConnectPersistentSubcription {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testConnectPersistentSub() throws Throwable {
        PersistentClient client = server.getPersistentClient();
        StreamsClient streamsClient = server.getStreamsClient();
        String streamName = "aStream-" + UUID.randomUUID().toString();

        PersistentSubscriptionSettings settings = PersistentSubscriptionSettings.builder().build();
        client.create(settings, streamName, "aGroup").get();

        List<ProposedEvent> events = new ArrayList<>();
        byte[] eventData = "{'foo': true}".getBytes();

        for (int i = 0; i < 3; ++i) {
            events.add(new ProposedEvent(UUID.randomUUID(), "foobar", "application/json", eventData, null));
        }

        final CompletableFuture<Integer> result = new CompletableFuture<>();
        streamsClient.appendToStream(streamName, ExpectedRevision.ANY, events).get();


        client.connect(streamName, "aGroup", 32, new PersistentSubscriptionListener() {
            private int count = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
                ++this.count;

                subscription.ack(event);

                if (this.count == 6) {
                    result.complete(this.count);
                    subscription.stop();
                }
            }

            @Override
            public void onError(PersistentSubscription subscription, Throwable throwable) {
                result.completeExceptionally(throwable);
            }

            @Override
            public void onCancelled(PersistentSubscription subscription) {
                result.complete(count);
            }
        }).get();

        events.clear();

        for (int i = 0; i < 3; ++i) {
            events.add(new ProposedEvent(UUID.randomUUID(), "foobar", "application/json", eventData, null));
        }

        streamsClient.appendToStream(streamName, ExpectedRevision.ANY, events).get();
        Assert.assertEquals(6, result.get().intValue());
    }
}

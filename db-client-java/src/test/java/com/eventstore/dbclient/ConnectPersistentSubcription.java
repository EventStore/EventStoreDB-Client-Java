package com.eventstore.dbclient;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ConnectPersistentSubcription {
    class Foo {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            com.eventstore.dbclient.ConnectPersistentSubcription.Foo foo1 = (com.eventstore.dbclient.ConnectPersistentSubcription.Foo) o;
            return foo == foo1.foo;
        }

        @Override
        public int hashCode() {
            return Objects.hash(foo);
        }

        private boolean foo;

        public boolean isFoo() {
            return foo;
        }

        public void setFoo(boolean foo) {
            this.foo = foo;
        }
    }

    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testConnectPersistentSub() throws Throwable {
        PersistentClient client = server.getPersistentClient();
        Streams streams = Streams.createWithDefaultCredentials(server.getConnectionNew(), "admin", "changeit");
        String streamName = "aStream-" + UUID.randomUUID().toString();

        PersistentSubscriptionSettings settings = PersistentSubscriptionSettings.builder().build();
        client.create(settings, streamName, "aGroup").get();

        ProposedEventBuilder builder = ProposedEvent.builderAsJson("foobar", new ConnectPersistentSubcription.Foo());
        AppendToStream appendCommand = streams.appendStream(streamName);

        List<ProposedEvent> events = new ArrayList<>();

        for (int i = 0; i < 3; ++i) {
            appendCommand.addEvent(builder.build());
        }

        final CompletableFuture<Integer> result = new CompletableFuture<>();

        appendCommand.execute().get();


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

        AppendToStream appendCommand2 = streams.appendStream(streamName);
        for (int i = 0; i < 3; ++i) {
            appendCommand2.addEvent(builder.build());
        }

        appendCommand2.execute().get();
        Assert.assertEquals(6, result.get().intValue());
    }
}

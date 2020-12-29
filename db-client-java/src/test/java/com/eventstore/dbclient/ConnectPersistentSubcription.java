package com.eventstore.dbclient;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.ArrayList;
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

    @Test
    public void testConnectPersistentSub() throws Throwable {
        PersistentSubscriptions persistent = server.getPersistentSubscriptionsAPI();
        Streams streams = server.getStreamsAPI();
        String streamName = "aStream-" + UUID.randomUUID().toString();

        persistent.create(streamName, "aGroup")
                .execute()
                .get();

        EventDataBuilder builder = EventData.builderAsJson("foobar", new ConnectPersistentSubcription.Foo());

        streams.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        final CompletableFuture<Integer> result = new CompletableFuture<>();


        persistent.connect(streamName, "aGroup", new PersistentSubscriptionListener() {
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
        }).execute(32).get();

        streams.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        Assert.assertEquals(6, result.get().intValue());
    }
}

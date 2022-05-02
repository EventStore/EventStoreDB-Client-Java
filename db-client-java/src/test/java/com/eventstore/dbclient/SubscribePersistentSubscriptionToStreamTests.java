package com.eventstore.dbclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SubscribePersistentSubscriptionToStreamTests extends ESDBTests {
    private EventStoreDBClient streamsClient;
    private EventStoreDBPersistentSubscriptionsClient client;

    @BeforeEach
    public void init() {
        streamsClient = getEmptyServer().getClient();
        client = getEmptyServer().getPersistentSubscriptionsClient();
    }

    class Foo {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubscribePersistentSubscriptionToStreamTests.Foo foo1 = (SubscribePersistentSubscriptionToStreamTests.Foo) o;
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

    @Test
    public void testSubscribePersistentSub() throws Throwable {
        String streamName = generateName();

        client.createToStream(streamName, "aGroup")
                .get();

        EventDataBuilder builder = EventData.builderAsJson("foobar", new SubscribePersistentSubscriptionToStreamTests.Foo());

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        final CompletableFuture<Integer> result = new CompletableFuture<>();

        SubscribePersistentSubscriptionOptions connectOptions = SubscribePersistentSubscriptionOptions.get()
                .bufferSize(32);

        client.subscribeToStream(streamName, "aGroup", connectOptions, new PersistentSubscriptionListener() {
            private int count = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
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

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        Assertions.assertEquals(6, result.get().intValue());
    }

    @Test
    public void testSubscribePersistentSubToAll() throws Throwable {
        String streamName = generateName();

        try {
            client.createToAll("aGroup")
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeatureException && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }

            return;
        }

        EventDataBuilder builder = EventData.builderAsJson("foobar", new SubscribePersistentSubscriptionToStreamTests.Foo());

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        final CompletableFuture<Integer> result = new CompletableFuture<>();

        SubscribePersistentSubscriptionOptions connectOptions = SubscribePersistentSubscriptionOptions.get()
                .bufferSize(32);

        client.subscribeToAll("aGroup", connectOptions, new PersistentSubscriptionListener() {
            private int count = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent resolvedEvent) {
                RecordedEvent event = resolvedEvent.getEvent();

                subscription.ack(resolvedEvent);

                if (!event.getEventType().equals("foobar"))
                    return;

                ++this.count;

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

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        Assertions.assertEquals(6, result.get().intValue());
    }
}

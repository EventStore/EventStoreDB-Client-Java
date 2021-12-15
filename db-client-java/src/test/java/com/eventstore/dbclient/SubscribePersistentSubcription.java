package com.eventstore.dbclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SubscribePersistentSubcription extends PersistentSubscriptionTestsBase {
    class Foo {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubscribePersistentSubcription.Foo foo1 = (SubscribePersistentSubcription.Foo) o;
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
        EventStoreDBClient streamsClient = server.getClient();
        String streamName = "aStream-" + UUID.randomUUID().toString();

        client.create(streamName, "aGroup")
                .get();

        EventDataBuilder builder = EventData.builderAsJson("foobar", new SubscribePersistentSubcription.Foo());

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        final CompletableFuture<Integer> result = new CompletableFuture<>();

        SubscribePersistentSubscriptionOptions connectOptions = SubscribePersistentSubscriptionOptions.get()
                .setBufferSize(32);

        client.subscribe(streamName, "aGroup", connectOptions, new PersistentSubscriptionListener() {
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

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        Assert.assertEquals(6, result.get().intValue());
    }

    @Test
    public void testSubscribePersistentSubToAll() throws Throwable {
        EventStoreDBClient streamsClient = server.getClient();
        String streamName = "aStream-" + UUID.randomUUID().toString();

        client.createToAll("aGroup")
                .get();

        EventDataBuilder builder = EventData.builderAsJson("foobar", new SubscribePersistentSubcription.Foo());

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        final CompletableFuture<Integer> result = new CompletableFuture<>();

        SubscribePersistentSubscriptionOptions connectOptions = SubscribePersistentSubscriptionOptions.get()
                .setBufferSize(32);

        client.subscribeToAll("aGroup", connectOptions, new PersistentSubscriptionListener() {
            private int count = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, ResolvedEvent resolvedEvent) {
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

        Assert.assertEquals(6, result.get().intValue());
    }

    @Test
    public void testPersistentSubscriptionFutureReturnsExecutionExceptionOnErrorDuringSubscribe() throws InterruptedException {
        EventStoreDBPersistentSubscriptionsClient client = server.getPersistentSubscriptionsClient();
        server.stop();

        try {
            SubscribePersistentSubscriptionOptions connectOptions = SubscribePersistentSubscriptionOptions.get()
                    .setBufferSize(32);
            client.subscribeToAll("unknown-group", connectOptions, new PersistentSubscriptionListener() {}).get();
            fail("Expected execution exception!");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            assertTrue(cause instanceof StatusRuntimeException);
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) cause;
            assertEquals(Status.UNAVAILABLE.getCode(), statusRuntimeException.getStatus().getCode());
        }
    }
}

package com.eventstore.dbclient.telemetry;

import com.eventstore.dbclient.*;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.ReadableSpan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public interface PersistentSubscriptionsTracingInstrumentationTests extends TelemetryAware {
    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testPersistentSubscriptionIsInstrumentedWithTracingAndRestoresRemoteContextAsExpected() throws Throwable {
        EventStoreDBClient streamsClient = getDefaultClient();
        EventStoreDBPersistentSubscriptionsClient psClient = getDefaultPersistentSubscriptionClient();

        String streamName = generateName();
        String groupName = "aGroup";

        EventData[] events = {
                EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                        .eventId(UUID.randomUUID())
                        .build(),
                EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                        .eventId(UUID.randomUUID())
                        .build()
        };

        CountDownLatch eventsLatch = new CountDownLatch(events.length);
        Exceptions exceptions = new Exceptions().registerGoAwayError();
        flaky(10, exceptions, () -> psClient.createToStream(streamName, groupName).get());

        streamsClient.appendToStream(streamName, events).get();

        PersistentSubscription subscription = psClient.subscribeToStream(streamName, groupName, SubscribePersistentSubscriptionOptions.get()
                .bufferSize(32), new PersistentSubscriptionListener() {
            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                subscription.ack(event);
                eventsLatch.countDown();
            }

            @Override
            public void onCancelled(PersistentSubscription subscription, Throwable throwable) {
                if (throwable == null) return;
                Assertions.fail(throwable);
            }
        }).get();

        eventsLatch.await();
        subscription.stop();

        List<ReadableSpan> appendSpans = getSpansForOperation(ClientTelemetryConstants.Operations.APPEND);
        Assertions.assertEquals(1, appendSpans.size());

        List<ReadableSpan> subscribeSpans = getSpansForOperation(ClientTelemetryConstants.Operations.SUBSCRIBE);
        Assertions.assertEquals(events.length, subscribeSpans.size());

        SpanContext appendSpanContext = appendSpans.get(0).getSpanContext();
        for (int i = 0; i < subscribeSpans.size(); i++) {
            ReadableSpan subscribeSpan = subscribeSpans.get(i);
            SpanContext parentSpanContext = subscribeSpan.getParentSpanContext();

            Assertions.assertNotNull(parentSpanContext);
            Assertions.assertEquals(appendSpanContext.getTraceId(), parentSpanContext.getTraceId());
            Assertions.assertEquals(appendSpanContext.getSpanId(), parentSpanContext.getSpanId());
            Assertions.assertTrue(parentSpanContext.isRemote());

            assertSubscriptionActivityHasExpectedAttributes(
                    subscribeSpan,
                    streamName,
                    subscription.getSubscriptionId(),
                    events[i].getEventId().toString(),
                    events[i].getEventType());
        }
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testPersistentSubscriptionInstrumentationHandlesExceptionsAsExpected() throws Throwable {
        EventStoreDBClient streamsClient = getDefaultClient();
        EventStoreDBPersistentSubscriptionsClient psClient = getDefaultPersistentSubscriptionClient();

        String streamName = generateName();
        String groupName = generateName();

        CountDownLatch eventsLatch = new CountDownLatch(1);
        RuntimeException expectedException = new RuntimeException("Oops! something went wrong...");

        Exceptions exceptions = new Exceptions().registerGoAwayError();
        flaky(10, exceptions, () -> psClient.createToStream(streamName, groupName).get());

        streamsClient.appendToStream(
                        streamName,
                        EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                                .eventId(UUID.randomUUID())
                                .build())
                .get();

        PersistentSubscription subscription = psClient.subscribeToStream(streamName, groupName, new PersistentSubscriptionListener() {
            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                try {
                    subscription.ack(event);
                    throw expectedException;
                } finally {
                    eventsLatch.countDown();
                }
            }

            @Override
            public void onCancelled(PersistentSubscription subscription, Throwable throwable) {
                if (throwable == null || throwable.equals(expectedException)) return;
                Assertions.fail(throwable);
            }
        }).get();

        eventsLatch.await();
        subscription.stop();

        List<ReadableSpan> subscribeSpans = getSpansForOperation(ClientTelemetryConstants.Operations.SUBSCRIBE);
        Assertions.assertEquals(1, subscribeSpans.size());

        assertErroneousSpanHasExpectedAttributes(subscribeSpans.get(0), expectedException);
    }
}

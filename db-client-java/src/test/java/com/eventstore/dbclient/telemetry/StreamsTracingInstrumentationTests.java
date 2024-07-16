package com.eventstore.dbclient.telemetry;

import com.eventstore.dbclient.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.ReadableSpan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public interface StreamsTracingInstrumentationTests extends TelemetryAware {
    @Test
    default void testAppendIsInstrumentedWithTracingAsExpected() throws Throwable {
        EventStoreDBClient client = getDefaultClient();
        String streamName = generateName();

        client.appendToStream(
                        streamName,
                        AppendToStreamOptions.get().expectedRevision(ExpectedRevision.noStream()),
                        EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                                .eventId(UUID.randomUUID())
                                .build())
                .get();

        List<ReadableSpan> spans = getSpansForOperation(ClientTelemetryConstants.Operations.APPEND);
        Assertions.assertEquals(1, spans.size());

        assertAppendSpanHasExpectedAttributes(spans.get(0), streamName);
    }

    @Test
    default void testTracingContextIsInjectedAsExpectedWhenUserMetadataIsJsonObject() throws Throwable {
        EventStoreDBClient client = getDefaultClient();
        String streamName = generateName();

        client.appendToStream(
                        streamName,
                        AppendToStreamOptions.get().expectedRevision(ExpectedRevision.noStream()),
                        EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                                .metadataAsBytes(mapper.writeValueAsBytes(new Foo()))
                                .eventId(UUID.randomUUID())
                                .build())
                .get();

        ReadResult readResult = client.readStream(streamName, ReadStreamOptions.get()).get();

        ResolvedEvent resolvedEvent = readResult.getEvents().get(0);
        Assertions.assertNotNull(resolvedEvent);

        ObjectNode userMetadata = mapper.readValue(resolvedEvent.getEvent().getUserMetadata(), ObjectNode.class);

        JsonNode traceIdNode = userMetadata.get(ClientTelemetryConstants.Metadata.TRACE_ID);
        JsonNode spanIdNode = userMetadata.get(ClientTelemetryConstants.Metadata.SPAN_ID);

        Assertions.assertNotNull(traceIdNode);
        Assertions.assertNotNull(spanIdNode);
    }

    @Test
    default void testTracingContextInjectionIsIgnoredAsExpectedWhenUserMetadataIsNonNullAndNotAJsonObject() throws Throwable {
        EventStoreDBClient client = getDefaultClient();
        String streamName = generateName();
        byte[] userMetadata = mapper.writeValueAsBytes("clearlynotvalidjson");

        client.appendToStream(
                        streamName,
                        AppendToStreamOptions.get().expectedRevision(ExpectedRevision.noStream()),
                        EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                                .metadataAsBytes(userMetadata)
                                .eventId(UUID.randomUUID())
                                .build())
                .get();

        ReadResult readResult = client.readStream(streamName, ReadStreamOptions.get()).get();

        ResolvedEvent resolvedEvent = readResult.getEvents().get(0);
        Assertions.assertNotNull(resolvedEvent);

        // Assert unchanged
        Assertions.assertArrayEquals(userMetadata, resolvedEvent.getEvent().getUserMetadata());
    }

    @Test
    default void testAppendInstrumentationHandlesExceptionsAsExpected() throws Throwable {
        EventStoreDBClient client = getDefaultClient();
        String streamName = generateName();

        WrongExpectedVersionException actualException = null;
        try {
            client.appendToStream(
                            streamName,
                            // Force WrongExpectedVersionException to be thrown.
                            AppendToStreamOptions.get().expectedRevision(ExpectedRevision.streamExists()),
                            EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                                    .eventId(UUID.randomUUID())
                                    .build())
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof WrongExpectedVersionException)
                actualException = (WrongExpectedVersionException) e.getCause();
        }

        // Ensure WrongExpectedVersionException was thrown.
        Assertions.assertNotNull(actualException);

        List<ReadableSpan> spans = getSpansForOperation(ClientTelemetryConstants.Operations.APPEND);
        Assertions.assertEquals(1, spans.size());

        assertErroneousSpanHasExpectedAttributes(spans.get(0), actualException);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testCatchupSubscriptionIsInstrumentedWithTracingAndRestoresRemoteContextAsExpected() throws Throwable {
        EventStoreDBClient client = getDefaultClient();
        String streamName = generateName();

        EventData[] events = {
                EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                        .eventId(UUID.randomUUID())
                        .build(),
                EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                        .eventId(UUID.randomUUID())
                        .build()
        };

        client.appendToStream(streamName, events).get();

        CountDownLatch subscribeSpansLatch = new CountDownLatch(events.length);
        onOperationSpanEnded(ClientTelemetryConstants.Operations.SUBSCRIBE, span -> subscribeSpansLatch.countDown());

        Subscription subscription = client.subscribeToStream(
                streamName,
                new SubscriptionListener() {
                }
        ).get();

        subscribeSpansLatch.await();
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
    default void testCatchupSubscriptionInstrumentationHandlesExceptionsAsExpected() throws Throwable {
        EventStoreDBClient client = getDefaultClient();
        String streamName = generateName();

        client.appendToStream(
                        streamName,
                        EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                                .eventId(UUID.randomUUID())
                                .build())
                .get();

        RuntimeException expectedException = new RuntimeException("Oops! something went wrong...");

        CountDownLatch subscribeSpansLatch = new CountDownLatch(1);
        onOperationSpanEnded(ClientTelemetryConstants.Operations.SUBSCRIBE, span -> subscribeSpansLatch.countDown());

        Subscription subscription = client.subscribeToStream(streamName, new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                throw expectedException;
            }

            @Override
            public void onCancelled(Subscription subscription, Throwable throwable) {
                if (throwable != null && !throwable.equals(expectedException))
                    Assertions.fail(throwable);
            }
        }).get();

        subscribeSpansLatch.await();
        subscription.stop();

        List<ReadableSpan> subscribeSpans = getSpansForOperation(ClientTelemetryConstants.Operations.SUBSCRIBE);
        Assertions.assertEquals(1, subscribeSpans.size());

        assertErroneousSpanHasExpectedAttributes(subscribeSpans.get(0), expectedException);
    }
}

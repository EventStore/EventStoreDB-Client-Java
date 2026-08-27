package io.kurrent.dbclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public interface TracingContextPropagationTests {
    String TRACE_ID = "0af7651916cd43dd8448eb211c80319c";
    String SPAN_ID = "b7ad6b7169203331";

    default Span spanWith(TraceFlags flags, TraceState traceState) {
        return Span.wrap(SpanContext.create(TRACE_ID, SPAN_ID, flags, traceState));
    }

    default ObjectNode parseMetadata(byte[] metadata) throws Exception {
        return new ObjectMapper().readValue(metadata, ObjectNode.class);
    }

    @Test
    default void testTracingContextIsInjectedForUnsampledSpans() throws Exception {
        Span span = spanWith(TraceFlags.getDefault(), TraceState.getDefault());

        ObjectNode metadata = parseMetadata(ClientTelemetry.tryInjectTracingContext(span, (byte[]) null));

        Assertions.assertEquals(
                "00-" + TRACE_ID + "-" + SPAN_ID + "-00",
                metadata.get(ClientTelemetryConstants.Metadata.TRACE_PARENT).asText());
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.TRACE_ID));
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.SPAN_ID));
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.TRACE_STATE));
    }

    @Test
    default void testTracingContextIsInjectedWithSampledFlagAndTraceState() throws Exception {
        TraceState traceState = TraceState.builder().put("dd", "s:1").build();
        Span span = spanWith(TraceFlags.getSampled(), traceState);

        ObjectNode metadata = parseMetadata(ClientTelemetry.tryInjectTracingContext(span, (byte[]) null));

        Assertions.assertEquals(
                "00-" + TRACE_ID + "-" + SPAN_ID + "-01",
                metadata.get(ClientTelemetryConstants.Metadata.TRACE_PARENT).asText());
        Assertions.assertEquals("dd=s:1", metadata.get(ClientTelemetryConstants.Metadata.TRACE_STATE).asText());
        Assertions.assertEquals(TRACE_ID, metadata.get(ClientTelemetryConstants.Metadata.TRACE_ID).asText());
        Assertions.assertEquals(SPAN_ID, metadata.get(ClientTelemetryConstants.Metadata.SPAN_ID).asText());
    }

    @Test
    default void testInjectionPreservesExistingUserMetadata() throws Exception {
        Span span = spanWith(TraceFlags.getSampled(), TraceState.getDefault());
        byte[] userMetadata = "{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8);

        ObjectNode metadata = parseMetadata(ClientTelemetry.tryInjectTracingContext(span, userMetadata));

        Assertions.assertEquals("bar", metadata.get("foo").asText());
        Assertions.assertNotNull(metadata.get(ClientTelemetryConstants.Metadata.TRACE_PARENT));
    }

    @Test
    default void testInjectionLeavesNonJsonObjectMetadataUntouched() {
        Span span = spanWith(TraceFlags.getSampled(), TraceState.getDefault());
        byte[] userMetadata = "clearlynotvalidjson".getBytes(StandardCharsets.UTF_8);

        byte[] result = ClientTelemetry.tryInjectTracingContext(span, userMetadata);

        Assertions.assertArrayEquals(userMetadata, result);
    }

    @Test
    default void testInjectionIsSkippedForInvalidSpanContext() {
        List<EventData> events = Collections.singletonList(
                EventData.builderAsJson("TestEvent", "{}".getBytes(StandardCharsets.UTF_8)).build());

        List<EventData> result = ClientTelemetry.tryInjectTracingContext(Span.getInvalid(), events);

        Assertions.assertSame(events, result);
    }

    @Test
    default void testInjectionIsSkippedForInvalidSpanContextOnRawMetadata() {
        byte[] userMetadata = "{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8);

        byte[] result = ClientTelemetry.tryInjectTracingContext(Span.getInvalid(), userMetadata);

        Assertions.assertSame(userMetadata, result);
    }

    @Test
    default void testInjectionOverwritesStaleTracingMetadata() throws Exception {
        String staleMetadata = "{"
                + "\"$traceParent\":\"00-11111111111111111111111111111111-1111111111111111-01\","
                + "\"$traceState\":\"dd=s:1\","
                + "\"$traceId\":\"11111111111111111111111111111111\","
                + "\"$spanId\":\"1111111111111111\""
                + "}";
        Span span = spanWith(TraceFlags.getDefault(), TraceState.getDefault());

        ObjectNode metadata = parseMetadata(ClientTelemetry.tryInjectTracingContext(
                span, staleMetadata.getBytes(StandardCharsets.UTF_8)));

        Assertions.assertEquals(
                "00-" + TRACE_ID + "-" + SPAN_ID + "-00",
                metadata.get(ClientTelemetryConstants.Metadata.TRACE_PARENT).asText());
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.TRACE_ID));
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.SPAN_ID));
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.TRACE_STATE));
    }

    @Test
    default void testExtractionPrefersTraceParentAndPreservesFlagsAndTraceState() {
        String metadata = "{"
                + "\"$traceParent\":\"00-" + TRACE_ID + "-" + SPAN_ID + "-00\","
                + "\"$traceState\":\"dd=s:1\","
                + "\"$traceId\":\"11111111111111111111111111111111\","
                + "\"$spanId\":\"1111111111111111\""
                + "}";

        SpanContext extracted = ClientTelemetry.tryExtractTracingContext(metadata.getBytes(StandardCharsets.UTF_8));

        Assertions.assertNotNull(extracted);
        Assertions.assertEquals(TRACE_ID, extracted.getTraceId());
        Assertions.assertEquals(SPAN_ID, extracted.getSpanId());
        Assertions.assertFalse(extracted.isSampled());
        Assertions.assertTrue(extracted.isRemote());
        Assertions.assertEquals("s:1", extracted.getTraceState().get("dd"));
    }

    @Test
    default void testExtractionFallsBackToLegacyFieldsAsSampled() {
        String metadata = "{\"$traceId\":\"" + TRACE_ID + "\",\"$spanId\":\"" + SPAN_ID + "\"}";

        SpanContext extracted = ClientTelemetry.tryExtractTracingContext(metadata.getBytes(StandardCharsets.UTF_8));

        Assertions.assertNotNull(extracted);
        Assertions.assertEquals(TRACE_ID, extracted.getTraceId());
        Assertions.assertEquals(SPAN_ID, extracted.getSpanId());
        Assertions.assertTrue(extracted.isSampled());
        Assertions.assertTrue(extracted.isRemote());
    }

    @Test
    default void testExtractionFallsBackToLegacyFieldsWhenTraceParentIsMalformed() {
        String metadata = "{"
                + "\"$traceParent\":\"not-a-traceparent\","
                + "\"$traceId\":\"" + TRACE_ID + "\","
                + "\"$spanId\":\"" + SPAN_ID + "\""
                + "}";

        SpanContext extracted = ClientTelemetry.tryExtractTracingContext(metadata.getBytes(StandardCharsets.UTF_8));

        Assertions.assertNotNull(extracted);
        Assertions.assertEquals(TRACE_ID, extracted.getTraceId());
        Assertions.assertEquals(SPAN_ID, extracted.getSpanId());
        Assertions.assertTrue(extracted.isSampled());
    }

    @Test
    default void testExtractionReturnsNullWhenNoTracingMetadataIsPresent() {
        Assertions.assertNull(ClientTelemetry.tryExtractTracingContext(null));
        Assertions.assertNull(ClientTelemetry.tryExtractTracingContext(
                "{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    default void testRoundTripPreservesSamplingDecisionAndTraceState() {
        TraceState traceState = TraceState.builder().put("dd", "s:0").build();
        Span span = spanWith(TraceFlags.getDefault(), traceState);

        byte[] metadata = ClientTelemetry.tryInjectTracingContext(span, (byte[]) null);
        SpanContext extracted = ClientTelemetry.tryExtractTracingContext(metadata);

        Assertions.assertNotNull(extracted);
        Assertions.assertEquals(TRACE_ID, extracted.getTraceId());
        Assertions.assertEquals(SPAN_ID, extracted.getSpanId());
        Assertions.assertFalse(extracted.isSampled());
        Assertions.assertEquals("s:0", extracted.getTraceState().get("dd"));
    }
}

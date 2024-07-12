package com.eventstore.dbclient;

import com.eventstore.dbclient.telemetry.PersistentSubscriptionsTracingInstrumentationTests;
import com.eventstore.dbclient.telemetry.SpanProcessorSpy;
import com.eventstore.dbclient.telemetry.StreamsTracingInstrumentationTests;
import com.eventstore.dbclient.telemetry.TracingContextInjectionTests;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TelemetryTests implements StreamsTracingInstrumentationTests, PersistentSubscriptionsTracingInstrumentationTests, TracingContextInjectionTests {
    static private Database database;
    static private Logger logger;

    private final List<Consumer<ReadableSpan>> spanEndedHooks = new ArrayList<>();
    private final List<ReadableSpan> recordedSpans = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(StreamsTests.class);
    }

    @BeforeEach
    public void beforeEach() {
        GlobalOpenTelemetry.resetForTest();
        spanEndedHooks.add(recordedSpans::add);

        OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider
                        .builder()
                        .addSpanProcessor(new SpanProcessorSpy(spanEndedHooks))
                        .build())
                .buildAndRegisterGlobal();
    }

    @Override
    public void onOperationSpanEnded(String operation, Consumer<ReadableSpan> onSpanEnded) {
        spanEndedHooks.add(span -> {
            if(Objects.equals(span.getAttribute(AttributeKey.stringKey(ClientTelemetryAttributes.Database.OPERATION)), operation))
                onSpanEnded.accept(span);
        });
    }

    @Override
    public List<ReadableSpan> getSpansForOperation(String operation) {
        return recordedSpans.stream()
                .filter(span -> {
                    String spanOperation = span.getAttribute(AttributeKey.stringKey(ClientTelemetryAttributes.Database.OPERATION));
                    return spanOperation != null && spanOperation.equals(operation);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @AfterAll
    public static void cleanup() {
        database.dispose();
    }
}

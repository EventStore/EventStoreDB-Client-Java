package com.eventstore.dbclient;

import com.eventstore.dbclient.telemetry.PersistentSubscriptionsTracingInstrumentationTests;
import com.eventstore.dbclient.telemetry.SpanProcessorSpy;
import com.eventstore.dbclient.telemetry.StreamsTracingInstrumentationTests;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TelemetryTests implements StreamsTracingInstrumentationTests, PersistentSubscriptionsTracingInstrumentationTests {
    static private List<ReadableSpan> recordedSpans;
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(StreamsTests.class);
        recordedSpans = new ArrayList<>();

        OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider
                        .builder()
                        .addSpanProcessor(SimpleSpanProcessor.create(InMemorySpanExporter.create()))
                        .addSpanProcessor(new SpanProcessorSpy(span -> recordedSpans.add(span)))
                        .build())
                .buildAndRegisterGlobal();
    }

    @BeforeEach
    public void beforeEach() {
        recordedSpans.clear();
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

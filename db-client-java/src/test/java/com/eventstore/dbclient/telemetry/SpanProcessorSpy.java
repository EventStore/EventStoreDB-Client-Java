package com.eventstore.dbclient.telemetry;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SpanProcessorSpy implements SpanProcessor {
    Consumer<ReadableSpan> consumer;

    public SpanProcessorSpy(Consumer<ReadableSpan> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onStart(@NotNull Context context, @NotNull ReadWriteSpan readWriteSpan) {
        // Do nothing.
    }

    @Override
    public boolean isStartRequired() {
        return false;
    }

    @Override
    public void onEnd(@NotNull ReadableSpan readableSpan) {
        consumer.accept(readableSpan);
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}

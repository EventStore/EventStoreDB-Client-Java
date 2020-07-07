package com.eventstore.dbclient;

import java.util.concurrent.TimeUnit;

public class TimeoutsBuilder {
    long shutdownTimeout;
    TimeUnit shutdownTimeoutUnit;

    long subscriptionTimeout;
    TimeUnit subscriptionTimeoutUnit;

    public static TimeoutsBuilder newBuilder() {
        TimeoutsBuilder builder = new TimeoutsBuilder();
        builder.shutdownTimeout = Timeouts.DEFAULT.shutdownTimeout;
        builder.shutdownTimeoutUnit = Timeouts.DEFAULT.shutdownTimeoutUnit;
        return builder;
    }

    public TimeoutsBuilder withShutdownTimeout(final long timeout, final TimeUnit timeoutUnit) {
        shutdownTimeout = timeout;
        shutdownTimeoutUnit = timeoutUnit;
        return this;
    }

    public TimeoutsBuilder withSubscriptionTimeout(final long timeout, final TimeUnit timeoutUnit) {
        subscriptionTimeout = timeout;
        subscriptionTimeoutUnit = timeoutUnit;
        return this;
    }

    public Timeouts build() {
        return new Timeouts(shutdownTimeout, shutdownTimeoutUnit, subscriptionTimeout, subscriptionTimeoutUnit);
    }

    private TimeoutsBuilder() {
        // Construct via newBuilder() factory method.
    }
}

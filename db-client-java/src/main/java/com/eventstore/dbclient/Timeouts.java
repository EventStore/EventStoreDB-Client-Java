package com.eventstore.dbclient;

import java.util.concurrent.TimeUnit;

public class Timeouts {
    final long shutdownTimeout;
    final TimeUnit shutdownTimeoutUnit;

    final long subscriptionTimeout;
    final TimeUnit subscriptionTimeoutUnit;

    public static final Timeouts DEFAULT = new Timeouts(
            5, TimeUnit.SECONDS,
            5, TimeUnit.SECONDS);

    Timeouts(final long shutdownTimeout, final TimeUnit shutdownTimeoutUnit,
             final long subscriptionTimeout, final TimeUnit subscriptionTimeoutUnit) {
        this.shutdownTimeout = shutdownTimeout;
        this.shutdownTimeoutUnit = shutdownTimeoutUnit;
        this.subscriptionTimeout = subscriptionTimeout;
        this.subscriptionTimeoutUnit = subscriptionTimeoutUnit;
    }
}

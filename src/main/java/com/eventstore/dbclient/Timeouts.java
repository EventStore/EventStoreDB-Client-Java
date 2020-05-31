package com.eventstore.dbclient;

import java.util.concurrent.TimeUnit;

public class Timeouts {
    final long shutdownTimeout;
    final TimeUnit shutdownTimeoutUnit;

    public static final Timeouts DEFAULT = new Timeouts(1, TimeUnit.SECONDS);

    Timeouts(final long shutdownTimeout, final TimeUnit shutdownTimeoutUnit) {
        this.shutdownTimeout = shutdownTimeout;
        this.shutdownTimeoutUnit = shutdownTimeoutUnit;
    }
}

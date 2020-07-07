package com.eventstore.dbclient;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TestTimeoutsBuilder {
    @Test
    public void testTimeoutsBuilder() {
        // Note these are NOT reasonable timeout values and are simply used
        // to test that the builder will construct.
        Timeouts timeouts = TimeoutsBuilder.newBuilder()
                .withShutdownTimeout(10, TimeUnit.HOURS)
                .withSubscriptionTimeout(9, TimeUnit.DAYS)
                .build();

        assertEquals(10, timeouts.shutdownTimeout);
        assertEquals(TimeUnit.HOURS, timeouts.shutdownTimeoutUnit);

        assertEquals(9, timeouts.subscriptionTimeout);
        assertEquals(TimeUnit.DAYS, timeouts.subscriptionTimeoutUnit);
    }
}

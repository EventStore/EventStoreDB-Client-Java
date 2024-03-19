package com.eventstore.dbclient;

/**
 * Options of the enable projection request.
 */
public class EnableProjectionOptions extends CallOptionsBase<EnableProjectionOptions> {
    private EnableProjectionOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static EnableProjectionOptions get() {
        return new EnableProjectionOptions();
    }
}

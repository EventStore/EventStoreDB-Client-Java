package com.eventstore.dbclient;

public class AbortProjectionOptions extends OptionsBase<AbortProjectionOptions> {
    private AbortProjectionOptions() {
    }

    public static AbortProjectionOptions get() {
        return new AbortProjectionOptions();
    }
}

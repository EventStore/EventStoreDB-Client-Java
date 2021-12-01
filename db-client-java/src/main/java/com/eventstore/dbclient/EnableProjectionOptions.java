package com.eventstore.dbclient;

public class EnableProjectionOptions extends OptionsBase<EnableProjectionOptions> {
    private EnableProjectionOptions() {
    }

    public static EnableProjectionOptions get() {
        return new EnableProjectionOptions();
    }
}

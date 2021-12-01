package com.eventstore.dbclient;

public class DisableProjectionOptions extends OptionsBase<DisableProjectionOptions> {
    private DisableProjectionOptions() {
    }

    public static DisableProjectionOptions get() {
        return new DisableProjectionOptions();
    }
}

package com.eventstore.dbclient;

public class ResetProjectionOptions extends OptionsBase<ResetProjectionOptions> {
    private ResetProjectionOptions() {
    }

    public static ResetProjectionOptions get() {
        return new ResetProjectionOptions();
    }
}

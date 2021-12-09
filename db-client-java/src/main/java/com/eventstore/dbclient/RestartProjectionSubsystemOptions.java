package com.eventstore.dbclient;

public class RestartProjectionSubsystemOptions extends OptionsBase<RestartProjectionSubsystemOptions> {
    private RestartProjectionSubsystemOptions() {
    }

    public static RestartProjectionSubsystemOptions get() {
        return new RestartProjectionSubsystemOptions();
    }
}

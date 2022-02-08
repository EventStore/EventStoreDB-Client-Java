package com.eventstore.dbclient;

public class RestartPersistentSubscriptionSubsystem extends OptionsBase<RestartPersistentSubscriptionSubsystem> {
    public static RestartPersistentSubscriptionSubsystem get() {
        return new RestartPersistentSubscriptionSubsystem();
    }
}

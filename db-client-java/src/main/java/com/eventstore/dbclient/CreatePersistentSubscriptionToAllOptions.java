package com.eventstore.dbclient;

public class CreatePersistentSubscriptionToAllOptions
        extends ManagePersistentSubscriptionOptionsBase<CreatePersistentSubscriptionToAllOptions, PersistentSubscriptionToAllSettings> {
    protected CreatePersistentSubscriptionToAllOptions() {
        super(PersistentSubscriptionToAllSettings.builder().build());
    }

    public static CreatePersistentSubscriptionToAllOptions get() {
        return new CreatePersistentSubscriptionToAllOptions();
    }
}

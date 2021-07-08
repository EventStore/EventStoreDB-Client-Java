package com.eventstore.dbclient;

public class CreatePersistentSubscriptionOptions
        extends ManagePersistentSubscriptionOptionsBase<CreatePersistentSubscriptionOptions, PersistentSubscriptionSettings> {
    protected CreatePersistentSubscriptionOptions() {
        super(PersistentSubscriptionSettings.builder().build());
    }

    public static CreatePersistentSubscriptionOptions get() {
        return new CreatePersistentSubscriptionOptions();
    }
}

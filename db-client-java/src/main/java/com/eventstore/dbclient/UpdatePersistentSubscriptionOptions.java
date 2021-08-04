package com.eventstore.dbclient;

public class UpdatePersistentSubscriptionOptions
        extends ManagePersistentSubscriptionOptionsBase<UpdatePersistentSubscriptionOptions, PersistentSubscriptionSettings> {
    protected UpdatePersistentSubscriptionOptions() {
        super(PersistentSubscriptionSettings.builder().build());
    }

    public static UpdatePersistentSubscriptionOptions get() {
        return new UpdatePersistentSubscriptionOptions();
    }
}

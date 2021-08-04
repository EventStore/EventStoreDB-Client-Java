package com.eventstore.dbclient;

public class UpdatePersistentSubscriptionToAllOptions
        extends ManagePersistentSubscriptionOptionsBase<UpdatePersistentSubscriptionToAllOptions, PersistentSubscriptionToAllSettings> {
    protected UpdatePersistentSubscriptionToAllOptions() {
        super(PersistentSubscriptionToAllSettings.builder().build());
    }

    public static UpdatePersistentSubscriptionToAllOptions get() {
        return new UpdatePersistentSubscriptionToAllOptions();
    }
}

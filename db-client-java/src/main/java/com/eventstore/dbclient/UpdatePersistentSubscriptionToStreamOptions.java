package com.eventstore.dbclient;

public class UpdatePersistentSubscriptionToStreamOptions
        extends ManagePersistentSubscriptionOptionsBase<UpdatePersistentSubscriptionToStreamOptions, PersistentSubscriptionToStreamSettings> {
    protected UpdatePersistentSubscriptionToStreamOptions() {
        super(PersistentSubscriptionToStreamSettings.builder().build());
    }

    public static UpdatePersistentSubscriptionToStreamOptions get() {
        return new UpdatePersistentSubscriptionToStreamOptions();
    }
}

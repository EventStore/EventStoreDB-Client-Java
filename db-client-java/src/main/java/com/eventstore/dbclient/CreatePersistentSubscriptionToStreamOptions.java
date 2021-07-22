package com.eventstore.dbclient;

public class CreatePersistentSubscriptionToStreamOptions
        extends ManagePersistentSubscriptionOptionsBase<CreatePersistentSubscriptionToStreamOptions, PersistentSubscriptionToStreamSettings> {
    protected CreatePersistentSubscriptionToStreamOptions() {
        super(PersistentSubscriptionToStreamSettings.builder().build());
    }

    public static CreatePersistentSubscriptionToStreamOptions get() {
        return new CreatePersistentSubscriptionToStreamOptions();
    }
}

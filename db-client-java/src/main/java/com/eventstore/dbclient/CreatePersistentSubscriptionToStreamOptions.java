package com.eventstore.dbclient;

public class CreatePersistentSubscriptionToStreamOptions
        extends AbstractPersistentSubscriptionSettingsBuilder<CreatePersistentSubscriptionToStreamOptions, PersistentSubscriptionToStreamSettings> {
    protected CreatePersistentSubscriptionToStreamOptions() {
        super(PersistentSubscriptionSettings.defaultRegular());
    }

    public static CreatePersistentSubscriptionToStreamOptions get() {
        return new CreatePersistentSubscriptionToStreamOptions();
    }

    public CreatePersistentSubscriptionToStreamOptions fromStart() {
        settings.setStartFrom(StreamPosition.start());
        return this;
    }

    public CreatePersistentSubscriptionToStreamOptions fromEnd() {
        settings.setStartFrom(StreamPosition.end());
        return this;
    }

    public CreatePersistentSubscriptionToStreamOptions startFrom(long revision) {
        settings.setStartFrom(StreamPosition.position(revision));
        return this;
    }
}

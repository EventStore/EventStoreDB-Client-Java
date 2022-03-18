package com.eventstore.dbclient;

public class UpdatePersistentSubscriptionToStreamOptions
        extends AbstractPersistentSubscriptionSettingsBuilder<UpdatePersistentSubscriptionToStreamOptions, PersistentSubscriptionToStreamSettings>  {
    protected UpdatePersistentSubscriptionToStreamOptions() {
        super(PersistentSubscriptionSettings.defaultRegular());
    }

    public static UpdatePersistentSubscriptionToStreamOptions get() {
        return new UpdatePersistentSubscriptionToStreamOptions();
    }

    public UpdatePersistentSubscriptionToStreamOptions fromStart() {
        settings.setStartFrom(StreamPosition.start());
        return this;
    }

    public UpdatePersistentSubscriptionToStreamOptions fromEnd() {
        settings.setStartFrom(StreamPosition.end());
        return this;
    }

    public UpdatePersistentSubscriptionToStreamOptions startFrom(long revision) {
        settings.setStartFrom(StreamPosition.position(revision));
        return this;
    }
}

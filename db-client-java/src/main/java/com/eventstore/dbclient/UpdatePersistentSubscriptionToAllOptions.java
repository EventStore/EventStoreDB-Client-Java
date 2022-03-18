package com.eventstore.dbclient;

public class UpdatePersistentSubscriptionToAllOptions
        extends AbstractPersistentSubscriptionSettingsBuilder<UpdatePersistentSubscriptionToAllOptions, PersistentSubscriptionToAllSettings> {
    protected UpdatePersistentSubscriptionToAllOptions() {
        super(PersistentSubscriptionSettings.defaultToAll());
    }

    public static UpdatePersistentSubscriptionToAllOptions get() {
        return new UpdatePersistentSubscriptionToAllOptions();
    }

    public UpdatePersistentSubscriptionToAllOptions fromStart() {
        this.settings.setStartFrom(StreamPosition.start());
        return this;
    }

    public UpdatePersistentSubscriptionToAllOptions fromEnd() {
        this.settings.setStartFrom(StreamPosition.end());
        return this;
    }

    public UpdatePersistentSubscriptionToAllOptions startFrom(Position position) {
        this.settings.setStartFrom(StreamPosition.position(position));
        return this;
    }

    public UpdatePersistentSubscriptionToAllOptions startFrom(long unsignedPrepare, long unsignedCommit) {
        this.settings.setStartFrom(StreamPosition.position(new Position(unsignedCommit, unsignedPrepare)));
        return this;
    }
}

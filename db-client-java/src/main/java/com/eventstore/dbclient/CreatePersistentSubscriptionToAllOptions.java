package com.eventstore.dbclient;

public class CreatePersistentSubscriptionToAllOptions
        extends AbstractPersistentSubscriptionSettingsBuilder<CreatePersistentSubscriptionToAllOptions, PersistentSubscriptionToAllSettings>  {

    private SubscriptionFilter filter;

    protected CreatePersistentSubscriptionToAllOptions() {
        super(PersistentSubscriptionSettings.defaultToAll());
    }

    public static CreatePersistentSubscriptionToAllOptions get() {
        return new CreatePersistentSubscriptionToAllOptions();
    }

    public SubscriptionFilter getFilter() {
        return filter;
    }

    public CreatePersistentSubscriptionToAllOptions filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }

    public CreatePersistentSubscriptionToAllOptions fromStart() {
        this.settings.setStartFrom(StreamPosition.start());
        return this;
    }

    public CreatePersistentSubscriptionToAllOptions fromEnd() {
        this.settings.setStartFrom(StreamPosition.end());
        return this;
    }

    public CreatePersistentSubscriptionToAllOptions startFrom(Position position) {
        this.settings.setStartFrom(StreamPosition.position(position));
        return this;
    }

    public CreatePersistentSubscriptionToAllOptions startFrom(long unsignedPrepare, long unsignedCommit) {
        this.settings.setStartFrom(StreamPosition.position(new Position(unsignedCommit, unsignedPrepare)));
        return this;
    }
}

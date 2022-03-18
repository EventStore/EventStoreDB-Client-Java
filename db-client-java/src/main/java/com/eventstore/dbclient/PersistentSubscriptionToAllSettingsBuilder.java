package com.eventstore.dbclient;

public class PersistentSubscriptionToAllSettingsBuilder
        extends AbstractPersistentSubscriptionSettingsBuilder<PersistentSubscriptionToAllSettingsBuilder, PersistentSubscriptionToAllSettings> {
    private SubscriptionFilter filter;

    public PersistentSubscriptionToAllSettingsBuilder() {
        super(PersistentSubscriptionSettings.defaultToAll());
    }

    public PersistentSubscriptionToAllSettingsBuilder(PersistentSubscriptionToAllSettings settings) {
        super(settings);
    }

    public PersistentSubscriptionSettings build() {
        return settings;
    }

    public PersistentSubscriptionToAllSettingsBuilder fromStart() {
        settings.setStartFrom(StreamPosition.start());
        return this;
    }

    public PersistentSubscriptionToAllSettingsBuilder fromEnd() {
        settings.setStartFrom(StreamPosition.end());
        return this;
    }

    /**
     * The exclusive position in the stream or transaction file the subscription should start from. Default: End of stream.
     */
    public PersistentSubscriptionToAllSettingsBuilder startFrom(long commitUnsigned, long prepareUnsigned) {
        return startFrom(new Position(commitUnsigned, prepareUnsigned));
    }

    /**
     * The exclusive position in the stream or transaction file the subscription should start from. Default: End of stream.
     */
    public PersistentSubscriptionToAllSettingsBuilder startFrom(Position value) {
        settings.setStartFrom(StreamPosition.position(value));
        return this;
    }

    public PersistentSubscriptionToAllSettingsBuilder filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }
}

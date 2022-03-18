package com.eventstore.dbclient;

public class PersistentSubscriptionSettingsBuilder
        extends AbstractPersistentSubscriptionSettingsBuilder<PersistentSubscriptionSettingsBuilder, PersistentSubscriptionToStreamSettings> {

    public PersistentSubscriptionSettingsBuilder() {
        super(PersistentSubscriptionSettings.defaultRegular());
    }

    public PersistentSubscriptionSettingsBuilder(PersistentSubscriptionToStreamSettings settings) {
        super(settings);
    }

    public PersistentSubscriptionSettings build() {
        return settings;
    }

    public PersistentSubscriptionSettingsBuilder fromStart() {
        return this.startFrom(StreamPosition.start());
    }

    public PersistentSubscriptionSettingsBuilder fromEnd() {
        return this.startFrom(StreamPosition.end());
    }

    /**
     * The exclusive position in the stream or transaction file the subscription should start from. Default: End of stream.
     */
    public PersistentSubscriptionSettingsBuilder startFrom(long value) {
        return this.startFrom(StreamPosition.position(value));
    }

    /**
     * The exclusive position in the stream or transaction file the subscription should start from. Default: End of stream.
     */
    public PersistentSubscriptionSettingsBuilder startFrom(StreamPosition<Long> value) {
        settings.setStartFrom(value);
        return this;
    }

    /**
     * @deprecated prefer {@link #startFrom(long)}
     */
    @Deprecated
    public PersistentSubscriptionSettingsBuilder revision(long value) {
        return this.startFrom(value);
    }

    /**
     * @deprecated prefer {@link #fromStart()}
     */
    @Deprecated
    public PersistentSubscriptionSettingsBuilder fromStreamStart() {
        return this.fromStart();
    }
}

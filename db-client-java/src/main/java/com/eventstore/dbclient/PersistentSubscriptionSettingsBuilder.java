package com.eventstore.dbclient;

public class PersistentSubscriptionSettingsBuilder
        extends AbstractPersistentSubscriptionSettingsBuilder<PersistentSubscriptionSettingsBuilder> {
    protected StreamRevision revision;

    public PersistentSubscriptionSettingsBuilder() {
        revision = StreamRevision.END;
    }

    public PersistentSubscriptionSettingsBuilder(PersistentSubscriptionSettings settings) {
        super(settings);

        revision = settings.getStreamRevision();
    }

    public PersistentSubscriptionSettings build() {
        return new PersistentSubscriptionSettings(checkpointAfterMs, extraStatistics, resolveLinkTos, historyBufferSize,
                liveBufferSize, checkPointUpperBound, maxRetryCount, maxSubscriberCount, messageTimeoutMs,
                checkPointLowerBound, readBatchSize, revision, consumerStrategyName);
    }

    public PersistentSubscriptionSettingsBuilder fromStart() {
        return this.startFrom(StreamRevision.START);
    }

    public PersistentSubscriptionSettingsBuilder fromEnd() {
        return this.startFrom(StreamRevision.END);
    }

    /**
     * The exclusive position in the stream or transaction file the subscription should start from. Default: End of stream.
     */
    public PersistentSubscriptionSettingsBuilder startFrom(long value) {
        return this.startFrom(new StreamRevision(value));
    }

    /**
     * The exclusive position in the stream or transaction file the subscription should start from. Default: End of stream.
     */
    public PersistentSubscriptionSettingsBuilder startFrom(StreamRevision value) {
        this.revision = value;
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

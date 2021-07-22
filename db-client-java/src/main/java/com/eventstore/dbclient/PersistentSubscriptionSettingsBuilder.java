package com.eventstore.dbclient;

/**
 * @deprecated prefer {@link PersistentSubscriptionToStreamSettingsBuilder}
 */
@Deprecated
public class PersistentSubscriptionSettingsBuilder
        extends AbstractPersistentSubscriptionSettingsBuilder<PersistentSubscriptionSettingsBuilder> {
    protected long revision;

    public PersistentSubscriptionSettingsBuilder() {
        revision = 0;
    }

    public PersistentSubscriptionSettingsBuilder(PersistentSubscriptionSettings settings) {
        super(settings);

        revision = settings.getRevision();
    }

    public PersistentSubscriptionSettings build() {
        return new PersistentSubscriptionSettings(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize,
                liveBufferSize, maxCheckpointCount, maxRetryCount, maxSubscriberCount, messageTimeoutMs,
                minCheckpointCount, readBatchSize, revision, strategy, fromStart, fromEnd);
    }

    public PersistentSubscriptionSettingsBuilder revision(long value) {
        this.revision = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder fromStreamStart() {
        return revision(0);
    }
}

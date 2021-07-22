package com.eventstore.dbclient;

public class PersistentSubscriptionToStreamSettingsBuilder
        extends AbstractPersistentSubscriptionSettingsBuilder<PersistentSubscriptionToStreamSettingsBuilder> {
    protected long revision;

    public PersistentSubscriptionToStreamSettingsBuilder() {
        revision = 0;
    }

    public PersistentSubscriptionToStreamSettingsBuilder(PersistentSubscriptionSettings settings) {
        super(settings);

        revision = settings.getRevision();
    }

    public PersistentSubscriptionToStreamSettingsBuilder(PersistentSubscriptionToStreamSettings settings) {
        super(settings);

        revision = settings.getRevision();
    }

    public PersistentSubscriptionToStreamSettings build() {
        return new PersistentSubscriptionToStreamSettings(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize,
                liveBufferSize, maxCheckpointCount, maxRetryCount, maxSubscriberCount, messageTimeoutMs,
                minCheckpointCount, readBatchSize, revision, strategy, fromStart, fromEnd);
    }

    public PersistentSubscriptionToStreamSettingsBuilder revision(long value) {
        this.revision = value;
        return this;
    }

    public PersistentSubscriptionToStreamSettingsBuilder fromStreamStart() {
        return revision(0);
    }
}

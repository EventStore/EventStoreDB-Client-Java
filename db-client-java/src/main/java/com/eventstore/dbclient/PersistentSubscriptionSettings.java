package com.eventstore.dbclient;

/**
 * @deprecated prefer {@link PersistentSubscriptionToStreamSettings}
 */
@Deprecated
public class PersistentSubscriptionSettings extends AbstractPersistentSubscriptionSettings {
    protected long revision;

    public PersistentSubscriptionSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
                                          int historyBufferSize, int liveBufferSize, int maxCheckpointCount,
                                          int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                          int minCheckpointCount, int readBatchSize, long revision,
                                          ConsumerStrategy strategy) {
        this(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize, liveBufferSize, maxCheckpointCount,
                maxRetryCount, maxSubscriberCount, messageTimeoutMs, minCheckpointCount, readBatchSize, revision,
                strategy, false, false);

    }

    public PersistentSubscriptionSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
                                          int historyBufferSize, int liveBufferSize, int maxCheckpointCount,
                                          int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                          int minCheckpointCount, int readBatchSize, long revision,
                                          ConsumerStrategy strategy, boolean fromStart, boolean fromEnd) {
        super(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize, liveBufferSize, maxCheckpointCount,
                maxRetryCount, maxSubscriberCount, messageTimeoutMs, minCheckpointCount, readBatchSize, strategy,
                fromStart, fromEnd);

        this.revision = revision;
    }

    public long getRevision() {
        return revision;
    }

    public static PersistentSubscriptionSettingsBuilder builder() {
        return new PersistentSubscriptionSettingsBuilder();
    }

    public static PersistentSubscriptionSettingsBuilder copy(PersistentSubscriptionSettings settings) {
        return new PersistentSubscriptionSettingsBuilder(settings);
    }
}

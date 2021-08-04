package com.eventstore.dbclient;

public class PersistentSubscriptionSettings extends AbstractPersistentSubscriptionSettings {
    protected StreamRevision revision;

    public PersistentSubscriptionSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
                                          int historyBufferSize, int liveBufferSize, int maxCheckpointCount,
                                          int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                          int minCheckpointCount, int readBatchSize, long revision,
                                          ConsumerStrategy strategy) {
        this(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize, liveBufferSize, maxCheckpointCount,
                maxRetryCount, maxSubscriberCount, messageTimeoutMs, minCheckpointCount, readBatchSize,
                new StreamRevision(revision), NamedConsumerStrategy.from(strategy));
    }

    public PersistentSubscriptionSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
                                          int historyBufferSize, int liveBufferSize, int maxCheckpointCount,
                                          int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                          int minCheckpointCount, int readBatchSize, StreamRevision revision,
                                          String consumerStrategyName) {
        super(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize, liveBufferSize, maxCheckpointCount,
                maxRetryCount, maxSubscriberCount, messageTimeoutMs, minCheckpointCount, readBatchSize, consumerStrategyName);

        this.revision = revision;
    }

    public long getRevision() {
        return revision.getValueUnsigned();
    }

    public StreamRevision getStreamRevision() {
        return revision;
    }

    public static PersistentSubscriptionSettingsBuilder builder() {
        return new PersistentSubscriptionSettingsBuilder();
    }

    public static PersistentSubscriptionSettingsBuilder copy(PersistentSubscriptionSettings settings) {
        return new PersistentSubscriptionSettingsBuilder(settings);
    }
}

package com.eventstore.dbclient;

public class PersistentSubscriptionToStreamSettings extends AbstractPersistentSubscriptionSettings {
    protected long revision;

    public PersistentSubscriptionToStreamSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
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

    public static PersistentSubscriptionToStreamSettingsBuilder builder() {
        return new PersistentSubscriptionToStreamSettingsBuilder();
    }

    /**
     * @deprecated prefer {@link #copy(PersistentSubscriptionToStreamSettings)}
     */
    @Deprecated
    public static PersistentSubscriptionToStreamSettingsBuilder copy(PersistentSubscriptionSettings settings) {
        return new PersistentSubscriptionToStreamSettingsBuilder(settings);
    }

    public static PersistentSubscriptionToStreamSettingsBuilder copy(PersistentSubscriptionToStreamSettings settings) {
        return new PersistentSubscriptionToStreamSettingsBuilder(settings);
    }
}

package com.eventstore.dbclient;

public class PersistentSubscriptionToAllSettings extends AbstractPersistentSubscriptionSettings {
    private Position position;

    public PersistentSubscriptionToAllSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
                                               int historyBufferSize, int liveBufferSize, int maxCheckpointCount,
                                               int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                               int minCheckpointCount, int readBatchSize, String strategy,
                                               Position position) {
        super(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize, liveBufferSize, maxCheckpointCount,
                maxRetryCount, maxSubscriberCount, messageTimeoutMs, minCheckpointCount, readBatchSize, strategy);

        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public static PersistentSubscriptionToAllSettingsBuilder builder() {
        return new PersistentSubscriptionToAllSettingsBuilder();
    }

    public static PersistentSubscriptionToAllSettingsBuilder copy(PersistentSubscriptionToAllSettings settings) {
        return new PersistentSubscriptionToAllSettingsBuilder(settings);
    }
}

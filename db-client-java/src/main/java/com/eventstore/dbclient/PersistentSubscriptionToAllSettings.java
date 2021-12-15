package com.eventstore.dbclient;

public class PersistentSubscriptionToAllSettings extends AbstractPersistentSubscriptionSettings {
    final private Position position;
    final private SubscriptionFilter filter;

    public PersistentSubscriptionToAllSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
                                               int historyBufferSize, int liveBufferSize, int maxCheckpointCount,
                                               int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                               int minCheckpointCount, int readBatchSize, String strategy,
                                               Position position, SubscriptionFilter filter) {
        super(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize, liveBufferSize, maxCheckpointCount,
                maxRetryCount, maxSubscriberCount, messageTimeoutMs, minCheckpointCount, readBatchSize, strategy);

        this.position = position;
        this.filter = filter;
    }

    public Position getPosition() {
        return position;
    }
    public SubscriptionFilter getFilter() {
        return filter;
    }

    public static PersistentSubscriptionToAllSettingsBuilder builder() {
        return new PersistentSubscriptionToAllSettingsBuilder();
    }

    public static PersistentSubscriptionToAllSettingsBuilder copy(PersistentSubscriptionToAllSettings settings) {
        return new PersistentSubscriptionToAllSettingsBuilder(settings);
    }
}

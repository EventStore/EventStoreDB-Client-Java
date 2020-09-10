package com.eventstore.dbclient;

public class PersistentSubscriptionSettings {
    private int checkpointAfterMs;
    private boolean extraStatistics;
    private boolean resolveLinks;
    private int historyBufferSize;
    private int liveBufferSize;
    private int maxCheckpointCount;
    private int maxRetryCount;
    private int maxSubscriberCount;
    private int messageTimeoutMs;
    private int minCheckpointCount;
    private int readBatchSize;
    private long revision;
    private ConsumerStrategy strategy;

    public PersistentSubscriptionSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks, int historyBufferSize, int liveBufferSize, int maxCheckpointCount, int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs, int minCheckpointCount, int readBatchSize, long revision, ConsumerStrategy strategy) {
        this.checkpointAfterMs = checkpointAfterMs;
        this.extraStatistics = extraStatistics;
        this.resolveLinks = resolveLinks;
        this.historyBufferSize = historyBufferSize;
        this.liveBufferSize = liveBufferSize;
        this.maxCheckpointCount = maxCheckpointCount;
        this.maxRetryCount = maxRetryCount;
        this.maxSubscriberCount = maxSubscriberCount;
        this.messageTimeoutMs = messageTimeoutMs;
        this.minCheckpointCount = minCheckpointCount;
        this.readBatchSize = readBatchSize;
        this.revision = revision;
        this.strategy = strategy;
    }

    public static PersistentSubscriptionSettingsBuilder builder() {
        return new PersistentSubscriptionSettingsBuilder();
    }

    public static PersistentSubscriptionSettingsBuilder copy(PersistentSubscriptionSettings settings) {
        return new PersistentSubscriptionSettingsBuilder(settings);
    }

    public int getCheckpointAfterMs() {
        return checkpointAfterMs;
    }

    public boolean isExtraStatistics() {
        return extraStatistics;
    }

    public boolean isResolveLinks() {
        return resolveLinks;
    }

    public int getHistoryBufferSize() {
        return historyBufferSize;
    }

    public int getLiveBufferSize() {
        return liveBufferSize;
    }

    public int getMaxCheckpointCount() {
        return maxCheckpointCount;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public int getMaxSubscriberCount() {
        return maxSubscriberCount;
    }

    public int getMessageTimeoutMs() {
        return messageTimeoutMs;
    }

    public int getMinCheckpointCount() {
        return minCheckpointCount;
    }

    public int getReadBatchSize() {
        return readBatchSize;
    }

    public long getRevision() {
        return revision;
    }

    public ConsumerStrategy getStrategy() {
        return strategy;
    }
}

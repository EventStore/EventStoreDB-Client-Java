package com.eventstore.dbclient;

public abstract class AbstractPersistentSubscriptionSettings<T> {
    private int checkpointAfterMs;
    private boolean extraStatistics;
    private boolean resolveLinks;
    private boolean fromStart;
    private boolean fromEnd;
    private int historyBufferSize;
    private int liveBufferSize;
    private int maxCheckpointCount;
    private int maxRetryCount;
    private int maxSubscriberCount;
    private int messageTimeoutMs;
    private int minCheckpointCount;
    private int readBatchSize;
    private ConsumerStrategy strategy;

    public AbstractPersistentSubscriptionSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
                                                  int historyBufferSize, int liveBufferSize, int maxCheckpointCount,
                                                  int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                                  int minCheckpointCount, int readBatchSize, ConsumerStrategy strategy,
                                                  boolean fromStart, boolean fromEnd) {
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
        this.fromStart = fromStart;
        this.fromEnd = fromEnd;
        this.strategy = strategy;
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

    public boolean getFromStart() {
        return fromStart;
    }

    public boolean getFromEnd() {
        return fromEnd;
    }

    public ConsumerStrategy getStrategy() {
        return strategy;
    }
}

package com.eventstore.dbclient;

import java.time.Duration;

public abstract class AbstractPersistentSubscriptionSettings<T> {
    private int checkpointAfterMs;
    private boolean extraStatistics;
    private boolean resolveLinkTos;
    private int historyBufferSize;
    private int liveBufferSize;
    private int checkPointUpperBound;
    private int maxRetryCount;
    private int maxSubscriberCount;
    private int messageTimeoutMs;
    private int checkPointLowerBound;
    private int readBatchSize;
    private String consumerStrategyName;

    public AbstractPersistentSubscriptionSettings(int checkpointAfterMs, boolean extraStatistics, boolean resolveLinks,
                                                  int historyBufferSize, int liveBufferSize, int checkPointUpperBound,
                                                  int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                                  int minCheckpointCount, int readBatchSize, String consumerStrategyName) {
        this.checkpointAfterMs = checkpointAfterMs;
        this.extraStatistics = extraStatistics;
        this.resolveLinkTos = resolveLinks;
        this.historyBufferSize = historyBufferSize;
        this.liveBufferSize = liveBufferSize;
        this.checkPointUpperBound = checkPointUpperBound;
        this.maxRetryCount = maxRetryCount;
        this.maxSubscriberCount = maxSubscriberCount;
        this.messageTimeoutMs = messageTimeoutMs;
        this.checkPointLowerBound = minCheckpointCount;
        this.readBatchSize = readBatchSize;
        this.consumerStrategyName = consumerStrategyName;
    }

    /**
     * The amount of time in milliseconds to try to checkpoint after.
     */
    public int getCheckpointAfterMs() {
        return checkpointAfterMs;
    }

    /**
     * The amount of time to try to checkpoint after.
     */
    public Duration getCheckpointAfter() {
        return Duration.ofMillis(checkpointAfterMs);
    }

    /**
     * Whether to track latency statistics on this subscription.
     */
    public boolean isExtraStatistics() {
        return extraStatistics;
    }

    /**
     * @deprecated prefer {@link #shouldResolveLinkTos()}
     */
    public boolean isResolveLinks() {
        return shouldResolveLinkTos();
    }

    /**
     * Whether the subscription should resolve linkTo events to their linked events.
     */
    public boolean shouldResolveLinkTos() {
        return resolveLinkTos;
    }

    /**
     * The number of events to cache when catching up. Default 500.
     */
    public int getHistoryBufferSize() {
        return historyBufferSize;
    }

    /**
     * The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default 500.
     */
    public int getLiveBufferSize() {
        return liveBufferSize;
    }

    /**
     * @deprecated prefer {@link #getCheckPointUpperBound()}
     */
    @Deprecated
    public int getMaxCheckpointCount() {
        return this.getCheckPointUpperBound();
    }

    /**
     * The maximum number of messages not checkpointed before forcing a checkpoint.
     */
    public int getCheckPointUpperBound() {
        return checkPointUpperBound;
    }

    /**
     * The maximum number of retries (due to timeout) before a message is considered to be parked.
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * The maximum number of subscribers allowed.
     */
    public int getMaxSubscriberCount() {
        return maxSubscriberCount;
    }

    /**
     * The amount of time after which to consider a message as timed out and retried.
     */
    public Duration getMessageTimeout() {
        return Duration.ofMillis(messageTimeoutMs);
    }

    /**
     * The amount of time in milliseconds after which to consider a message as timed out and retried.
     */
    public int getMessageTimeoutMs() {
        return messageTimeoutMs;
    }

    /**
     * @deprecated prefer {@link #getCheckPointLowerBound()}
     */
    @Deprecated
    public int getMinCheckpointCount() {
        return this.getCheckPointLowerBound();
    }

    /**
     * The minimum number of messages to process before a checkpoint may be written.
     */
    public int getCheckPointLowerBound() {
        return checkPointLowerBound;
    }

    /**
     * The number of events read at a time when catching up.
     */
    public int getReadBatchSize() {
        return readBatchSize;
    }

    /**
     * @deprecated prefer {@link #getConsumerStrategyName()}
     */
    @Deprecated
    public ConsumerStrategy getStrategy() throws Exception {

        switch (consumerStrategyName){
            case NamedConsumerStrategy.DISPATCH_TO_SINGLE: return ConsumerStrategy.DispatchToSingle;
            case NamedConsumerStrategy.ROUND_ROBIN: return ConsumerStrategy.RoundRobin;
            case NamedConsumerStrategy.PINNED: return ConsumerStrategy.Pinned;
        }

        throw new Exception("Non-default ConsumerStrategy specified, use getConsumerStrategyName()");
    }

    /**
     * The strategy to use for distributing events to client consumers.
     */
    public String getConsumerStrategyName() {
        return consumerStrategyName;
    }
}

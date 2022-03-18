package com.eventstore.dbclient;

import java.time.Duration;

public abstract class PersistentSubscriptionSettings {
    private int checkpointAfter;
    private boolean extraStatistics;
    private boolean resolveLinkTos;
    private int historyBufferSize;
    private int liveBufferSize;
    private int checkpointUpperBound;
    private int maxRetryCount;
    private int maxSubscriberCount;
    private int messageTimeoutMs;
    private int checkpointLowerBound;
    private int readBatchSize;
    private String consumerStrategyName;

    public PersistentSubscriptionSettings() {}

    public static PersistentSubscriptionToStreamSettings defaultRegular() {
        PersistentSubscriptionToStreamSettings settings = new PersistentSubscriptionToStreamSettings();
        defaultCommon(settings);

        settings.setStartFrom(StreamPosition.end());

        return settings;
    }

    public static PersistentSubscriptionToAllSettings defaultToAll() {
        PersistentSubscriptionToAllSettings settings = new PersistentSubscriptionToAllSettings();
        defaultCommon(settings);

        settings.setStartFrom(StreamPosition.end());

        return settings;
    }

    private static <A extends PersistentSubscriptionSettings> void defaultCommon(A settings) {
        settings.setMessageTimeoutMs(30_000);
        settings.setMaxRetryCount(10);
        settings.setLiveBufferSize(500);
        settings.setReadBatchSize(20);
        settings.setHistoryBufferSize(500);
        settings.setCheckpointAfter(2_000);
        settings.setCheckpointLowerBound(10);
        settings.setCheckpointUpperBound(1_000);
        settings.setMaxSubscriberCount(0);
        settings.setConsumerStrategyName(NamedConsumerStrategy.ROUND_ROBIN);
    }

    public PersistentSubscriptionSettings(int checkpointAfter, boolean extraStatistics, boolean resolveLinks,
                                          int historyBufferSize, int liveBufferSize, int checkpointUpperBound,
                                          int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                          int minCheckpointCount, int readBatchSize, String consumerStrategyName) {
        this.checkpointAfter = checkpointAfter;
        this.extraStatistics = extraStatistics;
        this.resolveLinkTos = resolveLinks;
        this.historyBufferSize = historyBufferSize;
        this.liveBufferSize = liveBufferSize;
        this.checkpointUpperBound = checkpointUpperBound;
        this.maxRetryCount = maxRetryCount;
        this.maxSubscriberCount = maxSubscriberCount;
        this.messageTimeoutMs = messageTimeoutMs;
        this.checkpointLowerBound = minCheckpointCount;
        this.readBatchSize = readBatchSize;
        this.consumerStrategyName = consumerStrategyName;
    }

    /**
     * The amount of time in milliseconds to try to checkpoint after.
     */
    public int getCheckpointAfterInMs() {
        return checkpointAfter;
    }

    /**
     * The amount of time to try to checkpoint after.
     */
    public Duration getCheckpointAfter() {
        return Duration.ofMillis(checkpointAfter);
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
     * @deprecated prefer {@link #getCheckpointUpperBound()}
     */
    @Deprecated
    public long getMaxCheckpointCount() {
        return this.getCheckpointUpperBound();
    }

    /**
     * The maximum number of messages not checkpointed before forcing a checkpoint.
     */
    public int getCheckpointUpperBound() {
        return checkpointUpperBound;
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
     * @deprecated prefer {@link #getCheckpointLowerBound()}
     */
    @Deprecated
    public long getMinCheckpointCount() {
        return this.getCheckpointLowerBound();
    }

    /**
     * The minimum number of messages to process before a checkpoint may be written.
     */
    public int getCheckpointLowerBound() {
        return checkpointLowerBound;
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

    public void setCheckpointAfter(int checkpointAfter) {
        this.checkpointAfter = checkpointAfter;
    }

    public void setExtraStatistics(boolean extraStatistics) {
        this.extraStatistics = extraStatistics;
    }

    public boolean isResolveLinkTos() {
        return resolveLinkTos;
    }

    public void setResolveLinkTos(boolean resolveLinkTos) {
        this.resolveLinkTos = resolveLinkTos;
    }

    public void setHistoryBufferSize(int historyBufferSize) {
        this.historyBufferSize = historyBufferSize;
    }

    public void setLiveBufferSize(int liveBufferSize) {
        this.liveBufferSize = liveBufferSize;
    }

    public void setCheckpointUpperBound(int checkpointUpperBound) {
        this.checkpointUpperBound = checkpointUpperBound;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setMaxSubscriberCount(int maxSubscriberCount) {
        this.maxSubscriberCount = maxSubscriberCount;
    }

    public void setMessageTimeoutMs(int messageTimeoutMs) {
        this.messageTimeoutMs = messageTimeoutMs;
    }

    public void setCheckpointLowerBound(int checkpointLowerBound) {
        this.checkpointLowerBound = checkpointLowerBound;
    }

    public void setReadBatchSize(int readBatchSize) {
        this.readBatchSize = readBatchSize;
    }

    public void setConsumerStrategyName(String consumerStrategyName) {
        this.consumerStrategyName = consumerStrategyName;
    }
}

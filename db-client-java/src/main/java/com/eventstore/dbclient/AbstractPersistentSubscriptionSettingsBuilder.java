package com.eventstore.dbclient;

import java.time.Duration;

public class AbstractPersistentSubscriptionSettingsBuilder<T> {
    protected int checkpointAfterMs;
    protected boolean extraStatistics;
    protected boolean resolveLinkTos;
    protected int historyBufferSize;
    protected int liveBufferSize;
    protected int checkPointUpperBound;
    protected int maxRetryCount;
    protected int maxSubscriberCount;
    protected int messageTimeoutMs;
    protected int checkPointLowerBound;
    protected int readBatchSize;
    protected String consumerStrategyName;

    public AbstractPersistentSubscriptionSettingsBuilder() {
        checkpointAfterMs = 2_000;
        resolveLinkTos = false;
        extraStatistics = false;
        messageTimeoutMs = 30_000;
        maxRetryCount = 10;
        checkPointLowerBound = 10;
        checkPointUpperBound = 1_000;
        maxSubscriberCount = 0;
        liveBufferSize = 500;
        readBatchSize = 20;
        historyBufferSize = 500;
        consumerStrategyName = NamedConsumerStrategy.ROUND_ROBIN;
    }

    public AbstractPersistentSubscriptionSettingsBuilder(AbstractPersistentSubscriptionSettings settings) {
        checkpointAfterMs = settings.getCheckpointAfterMs();
        resolveLinkTos = settings.shouldResolveLinkTos();
        extraStatistics = settings.isExtraStatistics();
        messageTimeoutMs = settings.getMessageTimeoutMs();
        maxRetryCount = settings.getMaxRetryCount();
        checkPointLowerBound = settings.getCheckPointLowerBound();
        checkPointUpperBound = settings.getCheckPointUpperBound();
        maxSubscriberCount = settings.getMaxSubscriberCount();
        liveBufferSize = settings.getLiveBufferSize();
        readBatchSize = settings.getReadBatchSize();
        historyBufferSize = settings.getHistoryBufferSize();
        consumerStrategyName = settings.getConsumerStrategyName();
    }

    /**
     * @deprecated prefer {@link #resolveLinkTos()}
     */
    @Deprecated
    public T enableLinkResolution() {
        return resolveLinks(true);
    }

    /**
     * @deprecated prefer {@link #notResolveLinkTos()}
     */
    @Deprecated
    public T disableLinkResolution() {
        return resolveLinks(false);
    }

    /**
     * @deprecated prefer {@link #resolveLinkTos(boolean)}
     */
    @Deprecated
    public T resolveLinks(boolean value) {
        return (T) resolveLinkTos(value);
    }

    /**
     * Whether the subscription should resolve linkTo events to their linked events. Default: false.
     */
    public T resolveLinkTos(boolean value) {
        this.resolveLinkTos = value;
        return (T) this;
    }

    /**
     * Resolve linkTo events to their linked events.
     */
    public T resolveLinkTos() {
        return this.resolveLinkTos(true);
    }

    /**
     * Don't resolve linkTo events to their linked events.
     */
    public T notResolveLinkTos() {
        return this.resolveLinkTos(false);
    }

    /**
     * Enable tracking of latency statistics on this subscription.
     */
    public T enableExtraStatistics() {
        return extraStatistics(true);
    }

    /**
     * Disable tracking of latency statistics on this subscription.
     */
    public T disableExtraStatistics() {
        return extraStatistics(false);
    }

    /**
     * Whether to track latency statistics on this subscription. Default: false.
     */
    public T extraStatistics(boolean value) {
        this.extraStatistics = value;
        return (T) this;
    }

    /**
     * The amount of time to try to checkpoint after. Default: 2 seconds.
     */
    public T checkpointAfter(Duration value) {
        this.checkpointAfterInMs((int)value.toMillis());
        return (T) this;
    }

    /**
     * The amount of time in milliseconds to try to checkpoint after. Default: 2 seconds.
     */
    public T checkpointAfterInMs(int value) {
        this.checkpointAfterMs = value;
        return (T) this;
    }

    /**
     * The number of events to cache when catching up. Default: 500.
     */
    public T historyBufferSize(int value) {
        this.historyBufferSize = value;
        return (T) this;
    }

    /**
     * The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default: 500.
     */
    public T liveBufferSize(int value) {
        this.liveBufferSize = value;
        return (T) this;
    }

    /**
     * @deprecated prefer {@link #checkPointUpperBound(int)}
     */
    @Deprecated
    public T maxCheckpointCount(int value) {
        return checkPointUpperBound(value);
    }

    /**
     * The maximum number of messages not checkpointed before forcing a checkpoint. Default: 1000.
     */
    public T checkPointUpperBound(int value) {
        this.checkPointUpperBound = value;
        return (T) this;
    }

    /**
     * @deprecated prefer {@link #checkPointLowerBound(int)}
     */
    @Deprecated
    public T minCheckpointCount(int value) {
        return this.checkPointLowerBound(checkPointLowerBound);
    }

    /**
     * The minimum number of messages to process before a checkpoint may be written. Default: 10.
     */
    public T checkPointLowerBound(int value) {
        this.checkPointLowerBound = value;
        return (T) this;
    }

    /**
     * The maximum number of subscribers allowed. Default: 0 (Unbounded).
     */
    public T maxSubscriberCount(int value) {
        this.maxSubscriberCount = value;
        return (T) this;
    }

    /**
     * The maximum number of retries (due to timeout) before a message is considered to be parked. Default: 10.
     */
    public T maxRetryCount(int value) {
        this.maxRetryCount = value;
        return (T) this;
    }

    /**
     * The amount of time after which to consider a message as timed out and retried. Default: 30 seconds.
     */
    public T messageTimeout(Duration value) {
        return this.messageTimeoutInMs((int)value.toMillis());
    }

    /**
     * The amount of time in milliseconds after which to consider a message as timed out and retried. Default: 30 seconds.
     */
    public T messageTimeoutInMs(int value) {
        this.messageTimeoutMs = value;
        return (T) this;
    }

    /**
     * The number of events read at a time when catching up. Default: 20.
     */
    public T readBatchSize(int value) {
        this.readBatchSize = value;
        return (T) this;
    }

    /**
     * The strategy to use for distributing events to client consumers.
     */
    public T consumerStrategy(ConsumerStrategy strategy) {
        this.consumerStrategyName = NamedConsumerStrategy.from(strategy);
        return (T) this;
    }

    /**
     * The strategy to use for distributing events to client consumers.
     */
    public T namedConsumerStrategy(String value) {
        this.consumerStrategyName = value;
        return (T) this;
    }
}

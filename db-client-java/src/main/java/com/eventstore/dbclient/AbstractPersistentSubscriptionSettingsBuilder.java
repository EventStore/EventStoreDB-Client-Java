package com.eventstore.dbclient;

import java.time.Duration;

public class AbstractPersistentSubscriptionSettingsBuilder<T, TSettings extends PersistentSubscriptionSettings> extends OptionsBase<T> {
    protected TSettings settings;

    public AbstractPersistentSubscriptionSettingsBuilder(TSettings settings) {
        this.settings = settings;
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
    @SuppressWarnings("unchecked")
    public T resolveLinks(boolean value) {
        return (T) resolveLinkTos(value);
    }

    /**
     * Whether the subscription should resolve linkTo events to their linked events. Default: false.
     */
    @SuppressWarnings("unchecked")
    public T resolveLinkTos(boolean value) {
        settings.setResolveLinkTos(value);
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
    @SuppressWarnings("unchecked")
    public T extraStatistics(boolean value) {
        settings.setExtraStatistics(value);
        return (T) this;
    }

    /**
     * The amount of time to try to checkpoint after. Default: 2 seconds.
     */
    @SuppressWarnings("unchecked")
    public T checkpointAfter(Duration value) {
        this.checkpointAfterInMs((int)value.toMillis());
        return (T) this;
    }

    /**
     * The amount of time in milliseconds to try to checkpoint after. Default: 2 seconds.
     */
    @SuppressWarnings("unchecked")
    public T checkpointAfterInMs(int value) {
        settings.setCheckpointAfter(value);
        return (T) this;
    }

    /**
     * The number of events to cache when catching up. Default: 500.
     */
    @SuppressWarnings("unchecked")
    public T historyBufferSize(int value) {
        settings.setHistoryBufferSize(value);
        return (T) this;
    }

    /**
     * The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default: 500.
     */
    @SuppressWarnings("unchecked")
    public T liveBufferSize(int value) {
        settings.setLiveBufferSize(value);
        return (T) this;
    }

    /**
     * @deprecated prefer {@link #checkpointUpperBound}
     */
    @Deprecated
    public T maxCheckpointCount(int value) {
        return checkpointUpperBound(value);
    }

    /**
     * The maximum number of messages not checkpointed before forcing a checkpoint. Default: 1000.
     */
    @SuppressWarnings("unchecked")
    public T checkpointUpperBound(int value) {
        settings.setCheckpointUpperBound(value);
        return (T) this;
    }

    /**
     * @deprecated prefer {@link #checkpointLowerBound}
     */
    @Deprecated
    public T minCheckpointCount(int value) {
        return this.checkpointLowerBound(value);
    }

    /**
     * The minimum number of messages to process before a checkpoint may be written. Default: 10.
     */
    @SuppressWarnings("unchecked")
    public T checkpointLowerBound(int value) {
        settings.setCheckpointLowerBound(value);
        return (T) this;
    }

    /**
     * The maximum number of subscribers allowed. Default: 0 (Unbounded).
     */
    @SuppressWarnings("unchecked")
    public T maxSubscriberCount(int value) {
        settings.setMaxSubscriberCount(value);
        return (T) this;
    }

    /**
     * The maximum number of retries (due to timeout) before a message is considered to be parked. Default: 10.
     */
    @SuppressWarnings("unchecked")
    public T maxRetryCount(int value) {
        settings.setMaxRetryCount(value);
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
    @SuppressWarnings("unchecked")
    public T messageTimeoutInMs(int value) {
        settings.setMessageTimeoutMs(value);
        return (T) this;
    }

    /**
     * The number of events read at a time when catching up. Default: 20.
     */
    @SuppressWarnings("unchecked")
    public T readBatchSize(int value) {
        settings.setReadBatchSize(value);
        return (T) this;
    }

    /**
     * The strategy to use for distributing events to client consumers.
     */
    @SuppressWarnings("unchecked")
    public T consumerStrategy(ConsumerStrategy strategy) {
        settings.setConsumerStrategyName(NamedConsumerStrategy.from(strategy));
        return (T) this;
    }

    /**
     * The strategy to use for distributing events to client consumers.
     */
    @SuppressWarnings("unchecked")
    public T namedConsumerStrategy(String value) {
        settings.setConsumerStrategyName(value);
        return (T) this;
    }

    public TSettings getSettings() {
        return settings;
    }
}

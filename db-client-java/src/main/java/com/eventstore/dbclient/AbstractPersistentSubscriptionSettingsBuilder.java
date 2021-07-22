package com.eventstore.dbclient;

public class AbstractPersistentSubscriptionSettingsBuilder<T> {
    protected int checkpointAfterMs;
    protected boolean extraStatistics;
    protected boolean resolveLinks;
    protected boolean fromStart;
    protected boolean fromEnd;
    protected int historyBufferSize;
    protected int liveBufferSize;
    protected int maxCheckpointCount;
    protected int maxRetryCount;
    protected int maxSubscriberCount;
    protected int messageTimeoutMs;
    protected int minCheckpointCount;
    protected int readBatchSize;
    protected ConsumerStrategy strategy;

    public AbstractPersistentSubscriptionSettingsBuilder() {
        checkpointAfterMs = 2_000;
        resolveLinks = false;
        extraStatistics = false;
        fromStart = false;
        fromEnd = false;
        messageTimeoutMs = 30_000;
        maxRetryCount = 10;
        minCheckpointCount = 10;
        maxCheckpointCount = 1_000;
        maxSubscriberCount = 0;
        liveBufferSize = 500;
        readBatchSize = 20;
        historyBufferSize = 500;
        strategy = ConsumerStrategy.RoundRobin;
    }

    public AbstractPersistentSubscriptionSettingsBuilder(AbstractPersistentSubscriptionSettings settings) {
        checkpointAfterMs = settings.getCheckpointAfterMs();
        resolveLinks = settings.isResolveLinks();
        extraStatistics = settings.isExtraStatistics();
        fromStart = settings.getFromStart();
        fromEnd = settings.getFromEnd();
        messageTimeoutMs = settings.getMessageTimeoutMs();
        maxRetryCount = settings.getMaxRetryCount();
        minCheckpointCount = settings.getMinCheckpointCount();
        maxCheckpointCount = settings.getMaxCheckpointCount();
        maxSubscriberCount = settings.getMaxSubscriberCount();
        liveBufferSize = settings.getLiveBufferSize();
        readBatchSize = settings.getReadBatchSize();
        historyBufferSize = settings.getHistoryBufferSize();
        strategy = settings.getStrategy();
    }

    public T enableLinkResolution() {
        return resolveLinks(true);
    }

    public T disableLinkResolution() {
        return resolveLinks(false);
    }

    public T resolveLinks(boolean value) {
        this.resolveLinks = value;
        return (T) this;
    }

    public T enableExtraStatistics() {
        return extraStatistics(true);
    }

    public T disableExtraStatistics() {
        return extraStatistics(false);
    }

    public T extraStatistics(boolean value) {
        this.extraStatistics = value;
        return (T) this;
    }

    public T checkpointAfterInMs(int value) {
        this.checkpointAfterMs = value;
        return (T) this;
    }

    public T fromStart() {
        this.fromStart = true;
        this.fromEnd = false;
        return (T) this;
    }

    public T fromEnd() {
        this.fromEnd = true;
        this.fromStart = false;
        return (T) this;
    }


    public T historyBufferSize(int value) {
        this.historyBufferSize = value;
        return (T) this;
    }

    public T liveBufferSize(int value) {
        this.liveBufferSize = value;
        return (T) this;
    }

    public T maxCheckpointCount(int value) {
        this.maxCheckpointCount = value;
        return (T) this;
    }

    public T minCheckpointCount(int value) {
        this.minCheckpointCount = value;
        return (T) this;
    }

    public T maxSubscriberCount(int value) {
        this.maxSubscriberCount = value;
        return (T) this;
    }

    public T maxRetryCount(int value) {
        this.maxRetryCount = value;
        return (T) this;
    }

    public T messageTimeoutInMs(int value) {
        this.messageTimeoutMs = value;
        return (T) this;
    }

    public T readBatchSize(int value) {
        this.readBatchSize = value;
        return (T) this;
    }

    public T consumerStrategy(ConsumerStrategy strategy) {
        this.strategy = strategy;
        return (T) this;
    }
}

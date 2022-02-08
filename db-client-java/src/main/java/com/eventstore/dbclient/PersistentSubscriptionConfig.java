package com.eventstore.dbclient;

public class PersistentSubscriptionConfig {
    private boolean resolveLinktos;
    private long startFrom;
    private long messageTimeoutMilliseconds;
    private boolean extraStatistics;
    private long maxRetryCount;
    private long liveBufferSize;
    private long bufferSize;
    private long readBatchSize;
    private boolean preferRoundRobin;
    private long checkPointAfterMilliseconds;
    private long minCheckPointCount;
    private long maxCheckPointCount;
    private long maxSubscriberCount;
    private ConsumerStrategy namedConsumerStrategy;

    public void setResolveLinktos(boolean resolveLinktos) {
        this.resolveLinktos = resolveLinktos;
    }

    public void setStartFrom(long startFrom) {
        this.startFrom = startFrom;
    }

    public void setMessageTimeoutMilliseconds(long messageTimeoutMilliseconds) {
        this.messageTimeoutMilliseconds = messageTimeoutMilliseconds;
    }

    public void setExtraStatistics(boolean extraStatistics) {
        this.extraStatistics = extraStatistics;
    }

    public void setMaxRetryCount(long maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setLiveBufferSize(long liveBufferSize) {
        this.liveBufferSize = liveBufferSize;
    }

    public void setBufferSize(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setReadBatchSize(long readBatchSize) {
        this.readBatchSize = readBatchSize;
    }

    public void setPreferRoundRobin(boolean preferRoundRobin) {
        this.preferRoundRobin = preferRoundRobin;
    }

    public void setCheckPointAfterMilliseconds(long checkPointAfterMilliseconds) {
        this.checkPointAfterMilliseconds = checkPointAfterMilliseconds;
    }

    public void setMinCheckPointCount(long minCheckPointCount) {
        this.minCheckPointCount = minCheckPointCount;
    }

    public void setMaxCheckPointCount(long maxCheckPointCount) {
        this.maxCheckPointCount = maxCheckPointCount;
    }

    public void setMaxSubscriberCount(long maxSubscriberCount) {
        this.maxSubscriberCount = maxSubscriberCount;
    }

    public void setNamedConsumerStrategy(ConsumerStrategy namedConsumerStrategy) {
        this.namedConsumerStrategy = namedConsumerStrategy;
    }

    public boolean isResolveLinktos() {
        return resolveLinktos;
    }

    public long getStartFrom() {
        return startFrom;
    }

    public long getMessageTimeoutMilliseconds() {
        return messageTimeoutMilliseconds;
    }

    public boolean isExtraStatistics() {
        return extraStatistics;
    }

    public long getMaxRetryCount() {
        return maxRetryCount;
    }

    public long getLiveBufferSize() {
        return liveBufferSize;
    }

    public long getBufferSize() {
        return bufferSize;
    }

    public long getReadBatchSize() {
        return readBatchSize;
    }

    public boolean isPreferRoundRobin() {
        return preferRoundRobin;
    }

    public long getCheckPointAfterMilliseconds() {
        return checkPointAfterMilliseconds;
    }

    public long getMinCheckPointCount() {
        return minCheckPointCount;
    }

    public long getMaxCheckPointCount() {
        return maxCheckPointCount;
    }

    public long getMaxSubscriberCount() {
        return maxSubscriberCount;
    }

    public ConsumerStrategy getNamedConsumerStrategy() {
        return namedConsumerStrategy;
    }
}

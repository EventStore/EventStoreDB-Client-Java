package com.eventstore.dbclient;

public abstract class PersistentSubscriptionStats {
    private int averagePerSecond;
    private long totalItems;
    private int countSinceLastMeasurement;
    private int readBufferCount;
    private long liveBufferCount;
    private int retryBufferCount;
    private int totalInFlightMessages;
    private int outstandingMessagesCount;
    private long parkedMessageCount;

    public int getAveragePerSecond() {
        return averagePerSecond;
    }

    public void setAveragePerSecond(int averagePerSecond) {
        this.averagePerSecond = averagePerSecond;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getCountSinceLastMeasurement() {
        return countSinceLastMeasurement;
    }

    public void setCountSinceLastMeasurement(int countSinceLastMeasurement) {
        this.countSinceLastMeasurement = countSinceLastMeasurement;
    }

    public int getReadBufferCount() {
        return readBufferCount;
    }

    public void setReadBufferCount(int readBufferCount) {
        this.readBufferCount = readBufferCount;
    }

    public long getLiveBufferCount() {
        return liveBufferCount;
    }

    public void setLiveBufferCount(long liveBufferCount) {
        this.liveBufferCount = liveBufferCount;
    }

    public int getRetryBufferCount() {
        return retryBufferCount;
    }

    public void setRetryBufferCount(int retryBufferCount) {
        this.retryBufferCount = retryBufferCount;
    }

    public int getTotalInFlightMessages() {
        return totalInFlightMessages;
    }

    public void setTotalInFlightMessages(int totalInFlightMessages) {
        this.totalInFlightMessages = totalInFlightMessages;
    }

    public int getOutstandingMessagesCount() {
        return outstandingMessagesCount;
    }

    public void setOutstandingMessagesCount(int outstandingMessagesCount) {
        this.outstandingMessagesCount = outstandingMessagesCount;
    }

    public long getParkedMessageCount() {
        return parkedMessageCount;
    }

    public void setParkedMessageCount(long parkedMessageCount) {
        this.parkedMessageCount = parkedMessageCount;
    }
}

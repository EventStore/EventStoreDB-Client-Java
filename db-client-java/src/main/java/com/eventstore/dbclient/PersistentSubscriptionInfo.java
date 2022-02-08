package com.eventstore.dbclient;

public class PersistentSubscriptionInfo {
    private String eventStreamId;
    private String groupName;
    private PersistentSubscriptionStatus status;
    private double averageItemsPerSecond;
    private long totalItemsProcessed;
    private long lastKnownEventNumber;
    private long connectionCount;
    private long totalInFlightMessages;
    private PersistentSubscriptionConfig config;

    public String getEventStreamId() {
        return eventStreamId;
    }

    public void setEventStreamId(String eventStreamId) {
        this.eventStreamId = eventStreamId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public PersistentSubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PersistentSubscriptionStatus status) {
        this.status = status;
    }

    public double getAverageItemsPerSecond() {
        return averageItemsPerSecond;
    }

    public void setAverageItemsPerSecond(double averageItemsPerSecond) {
        this.averageItemsPerSecond = averageItemsPerSecond;
    }

    public long getTotalItemsProcessed() {
        return totalItemsProcessed;
    }

    public void setTotalItemsProcessed(long totalItemsProcessed) {
        this.totalItemsProcessed = totalItemsProcessed;
    }

    public long getLastKnownEventNumber() {
        return lastKnownEventNumber;
    }

    public void setLastKnownEventNumber(long lastKnownEventNumber) {
        this.lastKnownEventNumber = lastKnownEventNumber;
    }

    public long getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(long connectionCount) {
        this.connectionCount = connectionCount;
    }

    public long getTotalInFlightMessages() {
        return totalInFlightMessages;
    }

    public void setTotalInFlightMessages(long totalInFlightMessages) {
        this.totalInFlightMessages = totalInFlightMessages;
    }

    public PersistentSubscriptionConfig getConfig() {
        return config;
    }

    public void setConfig(PersistentSubscriptionConfig config) {
        this.config = config;
    }
}

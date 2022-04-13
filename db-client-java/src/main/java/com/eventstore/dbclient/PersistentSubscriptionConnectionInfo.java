package com.eventstore.dbclient;

import java.util.Map;

public class PersistentSubscriptionConnectionInfo {
    private String from;
    private String username;
    private int averageItemsPerSecond;
    private long totalItems;
    private long countSinceLastMeasurement;
    private int availableSlots;
    private int inFlightMessages;
    private String connectionName;
    private Map<String, Long> extraStatistics;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getAverageItemsPerSecond() {
        return averageItemsPerSecond;
    }

    public void setAverageItemsPerSecond(int averageItemsPerSecond) {
        this.averageItemsPerSecond = averageItemsPerSecond;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getCountSinceLastMeasurement() {
        return countSinceLastMeasurement;
    }

    public void setCountSinceLastMeasurement(long countSinceLastMeasurement) {
        this.countSinceLastMeasurement = countSinceLastMeasurement;
    }

    public long getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public long getInFlightMessages() {
        return inFlightMessages;
    }

    public void setInFlightMessages(int inFlightMessages) {
        this.inFlightMessages = inFlightMessages;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public Map<String, Long> getExtraStatistics() {
        return extraStatistics;
    }

    public void setExtraStatistics(Map<String, Long> extraStatistics) {
        this.extraStatistics = extraStatistics;
    }
}

package com.eventstore.dbclient;

public class PersistentSubscriptionToStreamSettings extends PersistentSubscriptionSettings {
    private StreamPosition<Long> startFrom;
    public PersistentSubscriptionToStreamSettings() {}

    public StreamPosition<Long> getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(StreamPosition<Long> startFrom) {
        this.startFrom = startFrom;
    }
}

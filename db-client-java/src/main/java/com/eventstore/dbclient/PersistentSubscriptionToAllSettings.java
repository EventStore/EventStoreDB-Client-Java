package com.eventstore.dbclient;

public class PersistentSubscriptionToAllSettings extends PersistentSubscriptionSettings {
    private StreamPosition<Position> startFrom;
    public PersistentSubscriptionToAllSettings() {}

    public StreamPosition<Position> getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(StreamPosition<Position> startFrom) {
        this.startFrom = startFrom;
    }
}

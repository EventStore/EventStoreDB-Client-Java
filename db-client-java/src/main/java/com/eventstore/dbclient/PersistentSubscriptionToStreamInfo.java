package com.eventstore.dbclient;

public class PersistentSubscriptionToStreamInfo extends PersistentSubscriptionInfo {
    private PersistentSubscriptionToStreamSettings settings;
    private PersistentSubscriptionToStreamStats stats;

    public PersistentSubscriptionToStreamSettings getSettings() {
        return settings;
    }

    public void setSettings(PersistentSubscriptionToStreamSettings settings) {
        this.settings = settings;
    }

    public PersistentSubscriptionToStreamStats getStats() {
        return stats;
    }

    public void setStats(PersistentSubscriptionToStreamStats stats) {
        this.stats = stats;
    }
}

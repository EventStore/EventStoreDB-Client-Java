package com.eventstore.dbclient;

public class PersistentSubscriptionToAllInfo extends PersistentSubscriptionInfo {
    private PersistentSubscriptionToAllSettings settings;
    private PersistentSubscriptionToAllStats stats;

    public PersistentSubscriptionToAllSettings getSettings() {
        return settings;
    }

    public void setSettings(PersistentSubscriptionToAllSettings settings) {
        this.settings = settings;
    }

    public PersistentSubscriptionToAllStats getStats() {
        return stats;
    }

    public void setStats(PersistentSubscriptionToAllStats stats) {
        this.stats = stats;
    }
}

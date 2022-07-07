package com.eventstore.dbclient;

/**
 * Persistent subscription to $all info.
 */
public class PersistentSubscriptionToAllInfo extends PersistentSubscriptionInfo {
    private PersistentSubscriptionToAllSettings settings;
    private PersistentSubscriptionToAllStats stats;

    PersistentSubscriptionToAllInfo(){}

    /**
     * The settings used to create the persistent subscription.
     */
    public PersistentSubscriptionToAllSettings getSettings() {
        return settings;
    }

    void setSettings(PersistentSubscriptionToAllSettings settings) {
        this.settings = settings;
    }

    /**
     * Runtime persistent subscription statistics.
     */
    public PersistentSubscriptionToAllStats getStats() {
        return stats;
    }

    void setStats(PersistentSubscriptionToAllStats stats) {
        this.stats = stats;
    }
}

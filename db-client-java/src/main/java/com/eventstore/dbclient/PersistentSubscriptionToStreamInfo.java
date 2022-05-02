package com.eventstore.dbclient;

/**
 * Persistent subscription to stream info.
 */
public class PersistentSubscriptionToStreamInfo extends PersistentSubscriptionInfo {
    private PersistentSubscriptionToStreamSettings settings;
    private PersistentSubscriptionToStreamStats stats;

    PersistentSubscriptionToStreamInfo(){}

    /**
     * The settings used to create the persistent subscription.
     */
    public PersistentSubscriptionToStreamSettings getSettings() {
        return settings;
    }

    void setSettings(PersistentSubscriptionToStreamSettings settings) {
        this.settings = settings;
    }

    /**
     * Runtime persistent subscription statistics.
     */
    public PersistentSubscriptionToStreamStats getStats() {
        return stats;
    }

    void setStats(PersistentSubscriptionToStreamStats stats) {
        this.stats = stats;
    }
}

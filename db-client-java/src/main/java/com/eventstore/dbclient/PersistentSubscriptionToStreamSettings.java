package com.eventstore.dbclient;

/**
 * Persistent subscription to stream settings.
 */
public class PersistentSubscriptionToStreamSettings extends PersistentSubscriptionSettings {
    private StreamPosition<Long> startFrom;

    PersistentSubscriptionToStreamSettings() {}

    /**
     * Return a persistent subscription settings to $all with default properties.
     */
    static public PersistentSubscriptionToStreamSettings get() {
        return PersistentSubscriptionSettings.defaultRegular();
    }

    /**
     * Where to start subscription from. This can be from the start of the $all stream, from the end of the $all stream
     * at the time of creation, or from an inclusive position in $all stream.
     */
    public StreamPosition<Long> getStartFrom() {
        return startFrom;
    }

    /**
     * Where to start subscription from. This can be from the start of the $all stream, from the end of the $all stream
     * at the time of creation, or from an inclusive position in $all stream.
     */
    void setStartFrom(StreamPosition<Long> startFrom) {
        this.startFrom = startFrom;
    }
}

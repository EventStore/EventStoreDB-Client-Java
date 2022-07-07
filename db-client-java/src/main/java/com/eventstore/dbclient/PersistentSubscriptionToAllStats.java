package com.eventstore.dbclient;

import java.util.Optional;

/**
 * Processing-related persistent subscription to $all statistics.
 */
public class PersistentSubscriptionToAllStats extends PersistentSubscriptionStats {
    private Position lastCheckpointedEventPosition;
    private Position lastKnownEventPosition;

    PersistentSubscriptionToAllStats(){}

    /**
     * The transaction log position of the last checkpoint.
     */
    public Optional<Position> getLastCheckpointedEventPosition() {
        if (lastCheckpointedEventPosition == null)
            return Optional.empty();

        return Optional.of(lastCheckpointedEventPosition);
    }

    void setLastCheckpointedEventPosition(Position lastCheckpointedEventPosition) {
        this.lastCheckpointedEventPosition = lastCheckpointedEventPosition;
    }

    /**
     * The transaction log position of the last known event.
     */
    public Optional<Position> getLastKnownEventPosition() {
        if (lastKnownEventPosition == null)
            return Optional.empty();

        return Optional.of(lastKnownEventPosition);
    }

    void setLastKnownEventPosition(Position lastKnownEventPosition) {
        this.lastKnownEventPosition = lastKnownEventPosition;
    }
}

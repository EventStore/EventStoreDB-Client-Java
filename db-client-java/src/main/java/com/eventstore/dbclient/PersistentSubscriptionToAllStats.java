package com.eventstore.dbclient;

import java.util.Optional;

public class PersistentSubscriptionToAllStats extends PersistentSubscriptionStats {
    private Position lastCheckpointedEventPosition;
    private Position lastKnownEventPosition;

    public Optional<Position> getLastCheckpointedEventPosition() {
        if (lastCheckpointedEventPosition == null)
            return Optional.empty();

        return Optional.of(lastCheckpointedEventPosition);
    }

    public void setLastCheckpointedEventPosition(Position lastCheckpointedEventPosition) {
        this.lastCheckpointedEventPosition = lastCheckpointedEventPosition;
    }

    public Optional<Position> getLastKnownEventPosition() {
        if (lastKnownEventPosition == null)
            return Optional.empty();

        return Optional.of(lastKnownEventPosition);
    }

    public void setLastKnownEventPosition(Position lastKnownEventPosition) {
        this.lastKnownEventPosition = lastKnownEventPosition;
    }
}

package com.eventstore.dbclient;

import java.util.Optional;

public class PersistentSubscriptionToStreamStats extends PersistentSubscriptionStats {
    private Long lastCheckpointedEventRevision;
    private Long lastKnownEventRevision;

    public Optional<Long> getLastCheckpointedEventRevision() {
        if (lastCheckpointedEventRevision == null)
            return Optional.empty();

        return Optional.of(lastCheckpointedEventRevision);
    }

    public void setLastCheckpointedEventRevision(long lastCheckpointedEventRevision) {
        this.lastCheckpointedEventRevision = lastCheckpointedEventRevision;
    }

    public Optional<Long> getLastKnownEventRevision() {
        if (lastKnownEventRevision == null)
            return Optional.empty();

        return Optional.of(lastKnownEventRevision);
    }

    public void setLastKnownEventRevision(long lastKnownEventRevision) {
        this.lastKnownEventRevision = lastKnownEventRevision;
    }
}

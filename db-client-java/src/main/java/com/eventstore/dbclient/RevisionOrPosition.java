package com.eventstore.dbclient;

import java.util.Optional;

public class RevisionOrPosition {
    private Optional<Long> revision = Optional.empty();
    private Optional<Position> position = Optional.empty();

    public Optional<Long> getRevision() {
        return revision;
    }

    public Optional<Position> getPosition() {
        return position;
    }

    public void setRevision(long revision) {
        this.revision = Optional.of(revision);
    }

    public void setPosition(Position position) {
        this.position = Optional.of(position);
    }

    public boolean isRevisionPresent() {
        return revision.isPresent();
    }
}

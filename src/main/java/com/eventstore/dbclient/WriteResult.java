package com.eventstore.dbclient;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class WriteResult {
    private final StreamRevision nextExpectedRevision;
    private final Position logPosition;

    public WriteResult(@NotNull StreamRevision nextExpectedRevision, Position logPosition) {
        this.nextExpectedRevision = nextExpectedRevision;
        this.logPosition = logPosition;
    }

    public StreamRevision getNextExpectedRevision() {
        return nextExpectedRevision;
    }

    public Position getLogPosition() {
        return logPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WriteResult that = (WriteResult) o;
        return nextExpectedRevision.equals(that.nextExpectedRevision) &&
                logPosition.equals(that.logPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nextExpectedRevision, logPosition);
    }
}

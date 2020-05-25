package com.eventstore.dbclient.result;

import com.eventstore.dbclient.Position;
import com.eventstore.dbclient.StreamRevision;
import com.eventstore.dbclient.WriteResult;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class SuccessResult implements WriteResult {
    private final StreamRevision nextExpectedRevision;
    private final Position logPosition;

    public SuccessResult(@NotNull StreamRevision nextExpectedRevision, Position logPosition) {
        this.nextExpectedRevision = nextExpectedRevision;
        this.logPosition = logPosition;
    }

    @Override
    public StreamRevision getNextExpectedRevision() {
        return nextExpectedRevision;
    }

    @Override
    public Position getLogPosition() {
        return logPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuccessResult that = (SuccessResult) o;
        return nextExpectedRevision.equals(that.nextExpectedRevision) &&
                logPosition.equals(that.logPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nextExpectedRevision, logPosition);
    }
}

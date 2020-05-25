package com.eventstore.dbclient.result;

import com.eventstore.dbclient.Position;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class DeleteResult {
    private final Position logPosition;

    public DeleteResult(@NotNull Position logPosition) {
        this.logPosition = logPosition;
    }

    public Position getLogPosition() {
        return logPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteResult that = (DeleteResult) o;
        return logPosition.equals(that.logPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logPosition);
    }
}

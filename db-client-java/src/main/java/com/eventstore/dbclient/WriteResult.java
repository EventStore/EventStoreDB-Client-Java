package com.eventstore.dbclient;

import java.util.Objects;

/**
 * Returned after writing to a stream.
 */
public class WriteResult {
    private final long nextExpectedRevision;
    private final Position logPosition;

    WriteResult(long nextExpectedRevision, Position logPosition) {
        this.nextExpectedRevision = nextExpectedRevision;
        this.logPosition = logPosition;
    }

    /**
     * Next expected version of the stream.
     */
    public long getNextExpectedRevision() {
        return nextExpectedRevision;
    }

    /**
     * Transaction log position of the write.
     */
    public Position getLogPosition() {
        return logPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WriteResult that = (WriteResult) o;
        return nextExpectedRevision == that.nextExpectedRevision &&
                logPosition.equals(that.logPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nextExpectedRevision, logPosition);
    }
}

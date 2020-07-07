package com.eventstore.dbclient;

import javax.validation.constraints.NotNull;

public class WrongExpectedVersionException extends RuntimeException {
    private final String streamName;
    private final StreamRevision nextExpectedRevision;
    private final StreamRevision actualRevision;

    public WrongExpectedVersionException(
            @NotNull String streamName,
            @NotNull StreamRevision nextExpectedRevisionUnsigned,
            @NotNull StreamRevision actualRevision) {
        this.streamName = streamName;
        this.nextExpectedRevision = nextExpectedRevisionUnsigned;
        this.actualRevision = actualRevision;
    }

    public String getStreamName() {
        return streamName;
    }

    public StreamRevision getNextExpectedRevision() {
        return nextExpectedRevision;
    }

    public StreamRevision getActualVersion() {
        return actualRevision;
    }
}

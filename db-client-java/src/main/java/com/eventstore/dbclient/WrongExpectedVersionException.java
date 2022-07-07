package com.eventstore.dbclient;

import javax.validation.constraints.NotNull;

/**
 * When append request failed the optimistic concurrency on the server.
 */
public class WrongExpectedVersionException extends RuntimeException {
    private final String streamName;
    private final ExpectedRevision nextExpectedRevision;
    private final ExpectedRevision actualRevision;

    WrongExpectedVersionException(
            @NotNull String streamName,
            @NotNull ExpectedRevision nextExpectedRevisionUnsigned,
            @NotNull ExpectedRevision actualRevision) {
        this.streamName = streamName;
        this.nextExpectedRevision = nextExpectedRevisionUnsigned;
        this.actualRevision = actualRevision;
    }

    /**
     * Returns on which stream the error occurred.
     */
    public String getStreamName() {
        return streamName;
    }

    /**
     * Returns the expected revision by the request.
     */
    public ExpectedRevision getNextExpectedRevision() {
        return nextExpectedRevision;
    }

    /**
     * Returns the actual revision of the stream when the check was performed.
     */
    public ExpectedRevision getActualVersion() {
        return actualRevision;
    }
}

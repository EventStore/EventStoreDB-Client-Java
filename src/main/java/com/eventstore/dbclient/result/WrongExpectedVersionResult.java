package com.eventstore.dbclient.result;

import com.eventstore.dbclient.Position;
import com.eventstore.dbclient.StreamRevision;
import com.eventstore.dbclient.WriteResult;

import javax.validation.constraints.NotNull;

public class WrongExpectedVersionResult implements WriteResult {
    private final String streamName;
    private final StreamRevision nextExpectedRevision;
    private final StreamRevision actualRevision;
    private final Position logPosition;

    public WrongExpectedVersionResult(
            @NotNull String streamName,
            @NotNull StreamRevision nextExpectedRevisionUnsigned,
            @NotNull StreamRevision actualRevision) {
        this.streamName = streamName;
        this.nextExpectedRevision = nextExpectedRevisionUnsigned;
        this.actualRevision = actualRevision;
        this.logPosition = null;
    }

    public String getStreamName() {
        return streamName;
    }

    @Override
    public StreamRevision getNextExpectedRevision() {
        return nextExpectedRevision;
    }

    public StreamRevision getActualVersion() {
        return actualRevision;
    }

    @Override
    public Position getLogPosition() {
        return logPosition;
    }
}

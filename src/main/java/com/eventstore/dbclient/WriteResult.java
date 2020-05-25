package com.eventstore.dbclient;

public interface WriteResult {
    StreamRevision getNextExpectedRevision();
    Position getLogPosition();
}

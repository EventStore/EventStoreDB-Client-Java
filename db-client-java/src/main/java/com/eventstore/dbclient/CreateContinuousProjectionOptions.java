package com.eventstore.dbclient;

import io.grpc.Metadata;

public class CreateContinuousProjectionOptions {
    private final ConnectionMetadata metadata;
    private boolean trackEmittedStreams;

    private CreateContinuousProjectionOptions() {
        this.metadata = new ConnectionMetadata();
        this.trackEmittedStreams = false;
    }

    public static CreateContinuousProjectionOptions get() {
        return new CreateContinuousProjectionOptions();
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public CreateContinuousProjectionOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public boolean getTrackEmittedStreams() {
        return trackEmittedStreams;
    }

    public void trackEmittedStreams(boolean trackEmittedStreams) {
        this.trackEmittedStreams = trackEmittedStreams;
    }
}

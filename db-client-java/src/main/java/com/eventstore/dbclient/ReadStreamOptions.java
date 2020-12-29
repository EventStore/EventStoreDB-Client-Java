package com.eventstore.dbclient;

import io.grpc.Metadata;

public class ReadStreamOptions {
    private Timeouts timeouts;
    private final ConnectionMetadata metadata;

    private StreamRevision startRevision;
    private boolean resolveLinks;
    private Direction direction;

    private ReadStreamOptions() {
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();

        this.startRevision = StreamRevision.START;
        this.resolveLinks = false;
        this.direction = Direction.Forward;
    }

    public static ReadStreamOptions get() {
        return new ReadStreamOptions();
    }

    public Timeouts getTimeouts() {
        return this.timeouts;
    }

    public ReadStreamOptions timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public ReadStreamOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ReadStreamOptions forward() {
        this.direction = Direction.Forward;
        return this;
    }

    public ReadStreamOptions backward() {
        this.direction = Direction.Backward;
        return this;
    }

    public ReadStreamOptions requiresLeader() {
        return requiresLeader(true);
    }

    public ReadStreamOptions notRequireLeader() {
        return requiresLeader(false);
    }

    public ReadStreamOptions requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public boolean getResolveLinks() {
        return this.resolveLinks;
    }

    public ReadStreamOptions resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public ReadStreamOptions resolveLinks() {
        return this.resolveLinks(true);
    }

    public ReadStreamOptions notResolveLinks() {
        return this.resolveLinks(false);
    }

    public StreamRevision getStartingRevision() {
        return this.startRevision;
    }

    public ReadStreamOptions startingRevision(StreamRevision startRevision) {
        this.startRevision = startRevision;
        return this;
    }

    public ReadStreamOptions fromStart() {
        return this.startingRevision(StreamRevision.START);
    }

    public ReadStreamOptions fromEnd() {
        return this.startingRevision(StreamRevision.END);
    }

    public ReadStreamOptions fromRevision(long revision) {
        return this.startingRevision(new StreamRevision(revision));
    }
}

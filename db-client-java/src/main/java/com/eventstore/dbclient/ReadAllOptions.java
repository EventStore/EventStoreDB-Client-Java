package com.eventstore.dbclient;

import io.grpc.Metadata;

public class ReadAllOptions {
    private Timeouts timeouts;
    private final ConnectionMetadata metadata;

    private Position position;
    private boolean resolveLinks;
    private Direction direction;

    private ReadAllOptions() {
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();

        this.resolveLinks = false;
        this.position = Position.START;
        this.direction = Direction.Forward;
    }

    public static ReadAllOptions get() {
        return new ReadAllOptions();
    }

    public Timeouts getTimeouts() {
        return this.timeouts;
    }

    public ReadAllOptions timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public ReadAllOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ReadAllOptions forward() {
        this.direction = Direction.Forward;
        return this;
    }

    public ReadAllOptions backward() {
        this.direction = Direction.Backward;
        return this;
    }

    public ReadAllOptions requiresLeader() {
        return requiresLeader(true);
    }

    public ReadAllOptions notRequireLeader() {
        return requiresLeader(false);
    }

    public ReadAllOptions requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public boolean getResolveLinks() {
        return this.resolveLinks;
    }

    public ReadAllOptions resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public ReadAllOptions resolveLinks() {
        return this.resolveLinks(true);
    }

    public ReadAllOptions notResolveLinks() {
        return this.resolveLinks(false);
    }

    public Position getPosition() {
        return this.position;
    }

    public ReadAllOptions fromStart() {
        return this.fromPosition(Position.START);
    }

    public ReadAllOptions fromEnd() {
        return this.fromPosition(Position.END);
    }

    public ReadAllOptions fromPosition(Position position) {
        this.position = position;
        return this;
    }
}

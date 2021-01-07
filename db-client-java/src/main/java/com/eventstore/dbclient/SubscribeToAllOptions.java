package com.eventstore.dbclient;

import io.grpc.Metadata;

public class SubscribeToAllOptions {
    private Timeouts timeouts;
    private final ConnectionMetadata metadata;

    private boolean resolveLinks;
    private Position position;
    protected SubscriptionFilter filter;

    private SubscribeToAllOptions() {
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();

        this.resolveLinks = false;
        this.position = Position.START;
    }

    public static SubscribeToAllOptions get() {
        return new SubscribeToAllOptions();
    }

    public Timeouts getTimeouts() {
        return this.timeouts;
    }

    public SubscribeToAllOptions timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public SubscribeToAllOptions authenticated(UserCredentials credentials) {
        if(credentials == null)
            return this;

        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public boolean getResolveLinks() {
        return resolveLinks;
    }

    public SubscribeToAllOptions resolveLinks() {
        return this.resolveLinks(true);
    }

    public SubscribeToAllOptions notResolveLinks() {
        return this.resolveLinks(false);
    }

    public SubscribeToAllOptions resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public Position getPosition() {
        return position;
    }

    public SubscribeToAllOptions fromStart() {
        return this.fromPosition(Position.START);
    }

    public SubscribeToAllOptions fromEnd() {
        return this.fromPosition(Position.END);
    }

    public SubscribeToAllOptions fromPosition(Position position) {
        this.position = position;
        return this;
    }

    public SubscriptionFilter getFilter() {
        return filter;
    }

    public SubscribeToAllOptions filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }
}

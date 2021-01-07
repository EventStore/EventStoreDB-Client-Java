package com.eventstore.dbclient;

import io.grpc.Metadata;

public class SubscribeToStreamOptions {
    private Timeouts timeouts;
    private final ConnectionMetadata metadata;

    private StreamRevision startRevision;
    private boolean resolveLinks;

    private SubscribeToStreamOptions() {
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();

        this.resolveLinks = false;
        this.startRevision = StreamRevision.START;
    }

    public static SubscribeToStreamOptions get() {
        return new SubscribeToStreamOptions();
    }

    public Timeouts getTimeouts() {
        return this.timeouts;
    }

    public SubscribeToStreamOptions timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public SubscribeToStreamOptions authenticated(UserCredentials credentials) {
        if(credentials == null)
            return this;

        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public boolean getResolveLinks() {
        return this.resolveLinks;
    }

    public SubscribeToStreamOptions resolveLinks() {
        return this.resolveLinks(true);
    }

    public SubscribeToStreamOptions notResolveLinks() {
        return this.resolveLinks(false);
    }

    public SubscribeToStreamOptions resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public StreamRevision getStartingRevision() {
        return this.startRevision;
    }

    public SubscribeToStreamOptions startingRevision(StreamRevision startRevision) {
        this.startRevision = startRevision;
        return this;
    }

    public SubscribeToStreamOptions fromStart() {
        return this.startingRevision(StreamRevision.START);
    }

    public SubscribeToStreamOptions fromEnd() {
        return this.startingRevision(StreamRevision.END);
    }

    public SubscribeToStreamOptions fromRevision(long revision) {
        return this.startingRevision(new StreamRevision(revision));
    }


}

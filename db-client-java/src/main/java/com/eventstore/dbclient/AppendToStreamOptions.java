package com.eventstore.dbclient;

import io.grpc.Metadata;

public class AppendToStreamOptions {
    private Timeouts timeouts;
    private final ConnectionMetadata metadata;

    private ExpectedRevision expectedRevision;

    private AppendToStreamOptions() {
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();

        this.expectedRevision = ExpectedRevision.ANY;
    }

    public static AppendToStreamOptions get() {
        return new AppendToStreamOptions();
    }

    public Timeouts getTimeouts() {
        return this.timeouts;
    }

    public AppendToStreamOptions timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public AppendToStreamOptions authenticated(UserCredentials credentials) {
        if(credentials == null)
            return this;

        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public ExpectedRevision getExpectedRevision() {
        return this.expectedRevision;
    }

    public AppendToStreamOptions expectedRevision(ExpectedRevision revision) {
        this.expectedRevision = revision;
        return this;
    }

    public AppendToStreamOptions requiresLeader() {
        return requiresLeader(true);
    }

    public AppendToStreamOptions notRequireLeader() {
        return requiresLeader(false);
    }

    public AppendToStreamOptions requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }
}

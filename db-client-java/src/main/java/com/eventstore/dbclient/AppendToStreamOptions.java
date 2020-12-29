package com.eventstore.dbclient;

import io.grpc.Metadata;

public class AppendToStreamOptions {
    private ExpectedRevision expectedRevision;
    private Timeouts timeouts;
    private ConnectionMetadata metadata;

    private AppendToStreamOptions() {
        this.expectedRevision = ExpectedRevision.ANY;
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();
    }

    public static AppendToStreamOptions get(){
        return new AppendToStreamOptions();
    }

    public Metadata getMetadata(){
        return this.metadata.build();
    }

    public ExpectedRevision getExpectedRevision(){
        return this.expectedRevision;
    }

    public Timeouts getTimeouts(){
        return this.timeouts;
    }

    public AppendToStreamOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public AppendToStreamOptions expectedRevision(ExpectedRevision revision) {
        this.expectedRevision = revision;
        return this;
    }

    public AppendToStreamOptions timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
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

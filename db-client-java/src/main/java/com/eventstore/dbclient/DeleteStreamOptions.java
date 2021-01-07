package com.eventstore.dbclient;

import io.grpc.Metadata;

public class DeleteStreamOptions {
    private Timeouts timeouts;
    private final ConnectionMetadata metadata;

    private ExpectedRevision expectedRevision;
    private boolean softDelete;

    private DeleteStreamOptions() {
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();

        this.expectedRevision = ExpectedRevision.ANY;
        this.softDelete = true;
    }

    public static DeleteStreamOptions get() {
        return new DeleteStreamOptions();
    }

    public Timeouts getTimeouts() {
        return this.timeouts;
    }

    public DeleteStreamOptions timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public DeleteStreamOptions authenticated(UserCredentials credentials) {
        if(credentials == null)
            return this;

        this.metadata.authenticated(credentials);
        return this;
    }

    public DeleteStreamOptions requiresLeader() {
        return requiresLeader(true);
    }

    public DeleteStreamOptions notRequireLeader() {
        return requiresLeader(false);
    }

    public DeleteStreamOptions requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public boolean getSoftDelete() {
        return this.softDelete;
    }

    public DeleteStreamOptions softDelete() {
        this.softDelete = true;
        return this;
    }

    public DeleteStreamOptions hardDelete() {
        this.softDelete = false;
        return this;
    }

    public ExpectedRevision getExpectedRevision() {
        return this.expectedRevision;
    }

    public DeleteStreamOptions expectedRevision(ExpectedRevision revision) {
        this.expectedRevision = revision;
        return this;
    }
}

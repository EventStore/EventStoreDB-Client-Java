package com.eventstore.dbclient;

import io.grpc.Metadata;

class OptionsBase<T> {
    protected final ConnectionMetadata metadata;
    protected Long deadline;
    protected OperationKind kind;
    private UserCredentials credentials;
    private boolean requiresLeader;

    protected OptionsBase() {
        this.metadata = new ConnectionMetadata();
        this.kind = OperationKind.Regular;
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public String getUserCredentials() {
        return this.metadata.getUserCredentials();
    }

    @SuppressWarnings("unchecked")
    public T authenticated(UserCredentials credentials) {
        this.credentials = credentials;
        return (T)this;
    }

    public T requiresLeader() {
        return requiresLeader(true);
    }

    public T notRequireLeader() {
        return requiresLeader(false);
    }

    @SuppressWarnings("unchecked")
    public T requiresLeader(boolean value) {
        this.requiresLeader = value;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T deadline(long durationInMs) {
        deadline = durationInMs;

        return (T)this;
    }

    public Long getDeadline() {
        return deadline;
    }

    public OperationKind getKind() {
        return kind;
    }

    public boolean isLeaderRequired() {
        return this.requiresLeader;
    }

    public UserCredentials getCredentials() {
        return this.credentials;
    }
}

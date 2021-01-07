package com.eventstore.dbclient;

import io.grpc.Metadata;

class OptionsBase<T> {
    protected Timeouts timeouts;
    protected final ConnectionMetadata metadata;

    protected OptionsBase() {
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();
    }
    public Timeouts getTimeouts() {
        return this.timeouts;
    }

    public T timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return (T)this;
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public T authenticated(UserCredentials credentials) {
        if(credentials == null)
            return (T)this;

        this.metadata.authenticated(credentials);
        return (T)this;
    }
}

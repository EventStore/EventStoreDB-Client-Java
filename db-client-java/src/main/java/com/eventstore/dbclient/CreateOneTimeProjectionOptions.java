package com.eventstore.dbclient;

import io.grpc.Metadata;

public class CreateOneTimeProjectionOptions {
    private final ConnectionMetadata metadata;

    private CreateOneTimeProjectionOptions() {
        this.metadata = new ConnectionMetadata();
    }

    public static CreateOneTimeProjectionOptions get() {
        return new CreateOneTimeProjectionOptions();
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public CreateOneTimeProjectionOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }
}

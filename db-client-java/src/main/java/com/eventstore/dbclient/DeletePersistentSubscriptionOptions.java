package com.eventstore.dbclient;

import io.grpc.Metadata;

public class DeletePersistentSubscriptionOptions {
    private final ConnectionMetadata metadata;

    private DeletePersistentSubscriptionOptions() {
        this.metadata = new ConnectionMetadata();
    }

    public static DeletePersistentSubscriptionOptions get() {
        return new DeletePersistentSubscriptionOptions();
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public DeletePersistentSubscriptionOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }
}

package com.eventstore.dbclient;

import io.grpc.Metadata;

public class ConnectPersistentSubscriptionOptions {
    private final ConnectionMetadata metadata;

    private ConnectPersistentSubscriptionOptions() {
        this.metadata = new ConnectionMetadata();
    }

    public static ConnectPersistentSubscriptionOptions get() {
        return new ConnectPersistentSubscriptionOptions();
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public ConnectPersistentSubscriptionOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }
}

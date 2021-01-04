package com.eventstore.dbclient;

import io.grpc.Metadata;

public class SubscribePersistentSubscriptionOptions {
    private final ConnectionMetadata metadata;
    private int bufferSize;

    private SubscribePersistentSubscriptionOptions() {
        this.metadata = new ConnectionMetadata();
        this.bufferSize = 10;
    }

    public static SubscribePersistentSubscriptionOptions get() {
        return new SubscribePersistentSubscriptionOptions();
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public SubscribePersistentSubscriptionOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public SubscribePersistentSubscriptionOptions setBufferSize(int value) {
        bufferSize = value;
        return this;
    }
}

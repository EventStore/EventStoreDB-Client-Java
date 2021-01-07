package com.eventstore.dbclient;

import io.grpc.Metadata;

public class UpdatePersistentSubscriptionOptions {
    private final ConnectionMetadata metadata;
    private PersistentSubscriptionSettings settings;

    private UpdatePersistentSubscriptionOptions() {
        this.metadata = new ConnectionMetadata();
        this.settings = PersistentSubscriptionSettings.builder().build();
    }

    public static UpdatePersistentSubscriptionOptions get() {
        return new UpdatePersistentSubscriptionOptions();
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public UpdatePersistentSubscriptionOptions authenticated(UserCredentials credentials) {
        if(credentials == null)
            return this;

        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public PersistentSubscriptionSettings getSettings() {
        return settings;
    }

    public UpdatePersistentSubscriptionOptions settings(PersistentSubscriptionSettings settings) {
        this.settings = settings;

        return this;
    }
}

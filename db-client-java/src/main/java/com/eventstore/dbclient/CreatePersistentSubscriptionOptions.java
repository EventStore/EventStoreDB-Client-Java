package com.eventstore.dbclient;

import io.grpc.Metadata;

public class CreatePersistentSubscriptionOptions {
    private final ConnectionMetadata metadata;
    private PersistentSubscriptionSettings settings;

    private CreatePersistentSubscriptionOptions() {
        this.metadata = new ConnectionMetadata();
        this.settings = PersistentSubscriptionSettings.builder().build();
    }

    public static CreatePersistentSubscriptionOptions get() {
        return new CreatePersistentSubscriptionOptions();
    }

    public Metadata getMetadata() {
        return this.metadata.build();
    }

    public CreatePersistentSubscriptionOptions authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.hasUserCredentials();
    }

    public CreatePersistentSubscriptionOptions settings(PersistentSubscriptionSettings settings) {
        this.settings = settings;

        return this;
    }

    public PersistentSubscriptionSettings getSettings() {
        return settings;
    }
}

package com.eventstore.dbclient;

public class PersistentSubscriptions {
    private final GrpcClient client;
    private final UserCredentials credentials;

    public PersistentSubscriptions(GrpcClient client, UserCredentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    public CreatePersistentSubscription create(String stream, String group) {
        return this.create(stream, group, CreatePersistentSubscriptionOptions.get());
    }

    public CreatePersistentSubscription create(String stream, String group, PersistentSubscriptionSettings settings) {
        CreatePersistentSubscriptionOptions options = CreatePersistentSubscriptionOptions.get()
                .settings(settings);

        return this.create(stream, group, options);
    }

    public CreatePersistentSubscription create(String stream, String group, CreatePersistentSubscriptionOptions options) {
        return new CreatePersistentSubscription(this.client, stream, group, options);
    }

    public UpdatePersistentSubscription update(String stream, String group) {
        return this.update(stream, group, UpdatePersistentSubscriptionOptions.get());
    }

    public UpdatePersistentSubscription update(String stream, String group, PersistentSubscriptionSettings settings) {
        UpdatePersistentSubscriptionOptions options = UpdatePersistentSubscriptionOptions.get()
                .settings(settings);

        return this.update(stream, group, options);
    }

    public UpdatePersistentSubscription update(String stream, String group, UpdatePersistentSubscriptionOptions options) {
        return new UpdatePersistentSubscription(this.client, stream, group, options);
    }

    public DeletePersistentSubscription delete(String stream, String group) {
        return this.delete(stream, group, DeletePersistentSubscriptionOptions.get());
    }

    public DeletePersistentSubscription delete(String stream, String group, DeletePersistentSubscriptionOptions options) {
        return new DeletePersistentSubscription(this.client, stream, group, options);
    }

    public ConnectPersistentSubscription connect(String stream, String group, PersistentSubscriptionListener listener) {
        return this.connect(stream, group, listener, ConnectPersistentSubscriptionOptions.get());
    }

    public ConnectPersistentSubscription connect(String stream, String group, PersistentSubscriptionListener listener, ConnectPersistentSubscriptionOptions options) {
        return new ConnectPersistentSubscription(this.client, stream, group, listener, options);
    }
}

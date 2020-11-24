package com.eventstore.dbclient;

public class PersistentSubscriptions {
    private final GrpcClient client;
    private final UserCredentials credentials;

    public PersistentSubscriptions(GrpcClient client, UserCredentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    public CreatePersistentSubscription create(String stream, String group) {
        return new CreatePersistentSubscription(this.client, stream, group, this.credentials);
    }

    public UpdatePersistentSubscription update(String stream, String group) {
        return new UpdatePersistentSubscription(this.client, stream, group, this.credentials);
    }

    public DeletePersistentSubscription delete(String stream, String group) {
        return new DeletePersistentSubscription(this.client, stream, group, this.credentials);
    }

    public ConnectPersistentSubscription connect(String stream, String group, PersistentSubscriptionListener listener) {
        return new ConnectPersistentSubscription(this.client, stream, group, this.credentials, listener);
    }
}

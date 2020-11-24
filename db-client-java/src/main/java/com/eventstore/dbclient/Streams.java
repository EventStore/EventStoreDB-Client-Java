package com.eventstore.dbclient;

public class Streams {
    private final GrpcClient client;
    private final UserCredentials credentials;

    public Streams(GrpcClient client, UserCredentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    public AppendToStream appendStream(String streamName) {
        return new AppendToStream(this.client, streamName, this.credentials);
    }

    public ReadStream readStream(String streamName) {
        return new ReadStream(this.client, streamName, this.credentials);
    }

    public ReadAll readAll() {
        return new ReadAll(this.client, this.credentials);
    }

    public SubscribeToStream subscribeToStream(String streamName, SubscriptionListener listener) {
        return new SubscribeToStream(this.client, streamName, listener, this.credentials);
    }

    public SubscribeToAll subscribeToAll(SubscriptionListener listener) {
        return new SubscribeToAll(this.client, listener, this.credentials);
    }

    public DeleteStream deleteStream(String streamName) {
        return new DeleteStream(this.client, streamName, this.credentials);
    }
}

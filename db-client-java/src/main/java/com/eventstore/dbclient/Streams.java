package com.eventstore.dbclient;

public class Streams {
    private final GrpcClient client;
    private final UserCredentials credentials;

    public Streams(GrpcClient client, UserCredentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    public AppendToStream appendToStream(String streamName) {
        return this.appendToStream(streamName, null);
    }

    public AppendToStream appendToStream(String streamName, AppendToStreamOptions options) {
        if(options == null)
            options = AppendToStreamOptions.get();

        if(!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new AppendToStream(this.client, streamName, options);
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

package com.eventstore.dbclient;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class Streams {
    private final GrpcClient client;
    private final UserCredentials credentials;

    public Streams(GrpcClient client, UserCredentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, EventData... events) {
        return this.appendToStream(streamName, Arrays.stream(events).iterator());
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, Iterator<EventData> events) {
        return this.appendToStream(streamName, null, events);
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, AppendToStreamOptions options, EventData... events) {
        return this.appendToStream(streamName, options, Arrays.stream(events).iterator());
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, AppendToStreamOptions options, Iterator<EventData> events) {
        if (options == null)
            options = AppendToStreamOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new AppendToStream(this.client, streamName, events, options).execute();
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

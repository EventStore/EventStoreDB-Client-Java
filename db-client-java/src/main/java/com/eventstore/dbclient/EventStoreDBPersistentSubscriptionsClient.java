package com.eventstore.dbclient;

import java.util.concurrent.CompletableFuture;

public class EventStoreDBPersistentSubscriptionsClient extends EventStoreDBClientBase {
    private EventStoreDBPersistentSubscriptionsClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    public static EventStoreDBPersistentSubscriptionsClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBPersistentSubscriptionsClient(settings);
    }

    public CompletableFuture createToAll(String group) {
        return this.create(SystemStreams.ALL_STREAM, group);
    }

    public CompletableFuture create(String stream, String group) {
        return this.create(stream, group, CreatePersistentSubscriptionOptions.get());
    }

    public CompletableFuture createToAll(String group, PersistentSubscriptionSettings settings) {
        return this.create(SystemStreams.ALL_STREAM, group, settings);
    }

    public CompletableFuture create(String stream, String group, PersistentSubscriptionSettings settings) {
        CreatePersistentSubscriptionOptions options = CreatePersistentSubscriptionOptions.get()
                .settings(settings);

        return this.create(stream, group, options);
    }

    public CompletableFuture createToAll(String group, CreatePersistentSubscriptionOptions options) {
        return this.create(SystemStreams.ALL_STREAM, group, options);
    }

    public CompletableFuture create(String stream, String group, CreatePersistentSubscriptionOptions options) {
        if (options == null) {
            options = CreatePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        if (stream == SystemStreams.ALL_STREAM) {
            return new CreatePersistentSubscriptionToAll(this.client, group, options).execute();
        }

        return new CreatePersistentSubscriptionToStream(this.client, stream, group, options).execute();
    }

    public CompletableFuture updateToAll(String group) {
        return this.update(SystemStreams.ALL_STREAM, group);
    }

    public CompletableFuture update(String stream, String group) {
        return this.update(stream, group, UpdatePersistentSubscriptionOptions.get());
    }

    public CompletableFuture updateToAll(String group, PersistentSubscriptionSettings settings) {
        return this.update(SystemStreams.ALL_STREAM, group, settings);
    }

    public CompletableFuture update(String stream, String group, PersistentSubscriptionSettings settings) {
        UpdatePersistentSubscriptionOptions options = UpdatePersistentSubscriptionOptions.get()
                .settings(settings);

        return this.update(stream, group, options);
    }

    public CompletableFuture updateToAll(String group, UpdatePersistentSubscriptionOptions options) {
        return this.update(SystemStreams.ALL_STREAM, group, options);
    }

    public CompletableFuture update(String stream, String group, UpdatePersistentSubscriptionOptions options) {
        if (options == null) {
            options = UpdatePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        if (stream == SystemStreams.ALL_STREAM) {
            return new UpdatePersistentSubscriptionToAll(this.client, group, options).execute();
        }

        return new UpdatePersistentSubscriptionToStream(this.client, stream, group, options).execute();
    }

    public CompletableFuture deleteToAll(String group) {
        return this.delete(SystemStreams.ALL_STREAM, group);
    }

    public CompletableFuture delete(String stream, String group) {
        return this.delete(stream, group, DeletePersistentSubscriptionOptions.get());
    }

    public CompletableFuture deleteToAll(String group, DeletePersistentSubscriptionOptions options) {
        return this.delete(SystemStreams.ALL_STREAM, group, options);
    }

    public CompletableFuture delete(String stream, String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        if (stream == SystemStreams.ALL_STREAM) {
            return new DeletePersistentSubscriptionToAll(this.client, group, options).execute();
        }

        return new DeletePersistentSubscriptionToStream(this.client, stream, group, options).execute();
    }

    public CompletableFuture subscribeToAll(String group, PersistentSubscriptionListener listener) {
        return this.subscribe(SystemStreams.ALL_STREAM, group, listener);
    }

    public CompletableFuture subscribe(String stream, String group, PersistentSubscriptionListener listener) {
        return this.subscribe(stream, group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    public CompletableFuture subscribeToAll(String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        return this.subscribe(SystemStreams.ALL_STREAM, group, options, listener);
    }

    public CompletableFuture subscribe(String stream, String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null) {
            options = SubscribePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        if (stream == SystemStreams.ALL_STREAM) {
            return new SubscribePersistentSubscriptionToAll(this.client, group, options, listener).execute();
        }

        return new SubscribePersistentSubscriptionToStream(this.client, stream, group, options, listener).execute();
    }
}

package com.eventstore.dbclient;

import java.util.concurrent.CompletableFuture;

public class EventStoreDBPersistentSubscriptionsClient extends EventStoreDBClientBase {
    private EventStoreDBPersistentSubscriptionsClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    public static EventStoreDBPersistentSubscriptionsClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBPersistentSubscriptionsClient(settings);
    }

    public CompletableFuture create(String stream, String group) {
        return this.create(stream, group, CreatePersistentSubscriptionOptions.get());
    }

    public CompletableFuture create(String stream, String group, PersistentSubscriptionSettings settings) {
        CreatePersistentSubscriptionOptions options = CreatePersistentSubscriptionOptions.get()
                .settings(settings);

        return this.create(stream, group, options);
    }

    public CompletableFuture create(String stream, String group, CreatePersistentSubscriptionOptions options) {
        if (options == null)
            options = CreatePersistentSubscriptionOptions.get();

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new CreatePersistentSubscription(this.client, stream, group, options).execute();
    }

    public CompletableFuture update(String stream, String group) {
        return this.update(stream, group, UpdatePersistentSubscriptionOptions.get());
    }

    public CompletableFuture update(String stream, String group, PersistentSubscriptionSettings settings) {
        UpdatePersistentSubscriptionOptions options = UpdatePersistentSubscriptionOptions.get()
                .settings(settings);

        return this.update(stream, group, options);
    }

    public CompletableFuture update(String stream, String group, UpdatePersistentSubscriptionOptions options) {
        if (options == null)
            options = UpdatePersistentSubscriptionOptions.get();

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new UpdatePersistentSubscription(this.client, stream, group, options).execute();
    }

    public CompletableFuture delete(String stream, String group) {
        return this.delete(stream, group, DeletePersistentSubscriptionOptions.get());
    }

    public CompletableFuture delete(String stream, String group, DeletePersistentSubscriptionOptions options) {
        if (options == null)
            options = DeletePersistentSubscriptionOptions.get();

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new DeletePersistentSubscription(this.client, stream, group, options).execute();
    }

    public CompletableFuture subscribe(String stream, String group, PersistentSubscriptionListener listener) {
        return this.subscribe(stream, group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    public CompletableFuture subscribe(String stream, String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null)
            options = SubscribePersistentSubscriptionOptions.get();

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new SubscribePersistentSubscription(this.client, stream, group, options, listener).execute();
    }
}

package com.eventstore.dbclient;

import java.util.concurrent.CompletableFuture;

public class EventStoreDBPersistentSubscriptionsClient extends EventStoreDBClientBase {
    private EventStoreDBPersistentSubscriptionsClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    public static EventStoreDBPersistentSubscriptionsClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBPersistentSubscriptionsClient(settings);
    }

    /**
     * @deprecated prefer {@link #createToStream(String, String)}
     */
    @Deprecated
    public CompletableFuture create(String stream, String group) {
        return this.createToStream(stream, group);
    }

    public CompletableFuture createToAll(String group) {
        return this.createToAll(group, CreatePersistentSubscriptionToAllOptions.get());
    }

    public CompletableFuture createToStream(String stream, String group) {
        return this.createToStream(stream, group, CreatePersistentSubscriptionToStreamOptions.get());
    }

    /**
     * @deprecated prefer {@link #createToStream(String, String, PersistentSubscriptionToStreamSettings)}
     */
    @Deprecated
    public CompletableFuture create(String stream, String group, PersistentSubscriptionSettings settings) {
        return this.createToStream(stream, group, PersistentSubscriptionToStreamSettings.copy(settings).build());
    }

    public CompletableFuture createToAll(String group, PersistentSubscriptionToAllSettings settings) {
        CreatePersistentSubscriptionToAllOptions options = CreatePersistentSubscriptionToAllOptions.get()
                .settings(settings);

        return this.createToAll(group, options);
    }

    public CompletableFuture createToStream(String stream, String group, PersistentSubscriptionToStreamSettings settings) {
        CreatePersistentSubscriptionToStreamOptions options = CreatePersistentSubscriptionToStreamOptions.get()
                .settings(settings);

        return this.createToStream(stream, group, options);
    }

    /**
     * @deprecated prefer {@link #createToStream(String, String, CreatePersistentSubscriptionToStreamOptions)}
     */
    @Deprecated
    public CompletableFuture create(String stream, String group, CreatePersistentSubscriptionOptions options) {
        if (options == null) {
            options = CreatePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new CreatePersistentSubscription(this.client, stream, group, options).execute();
    }

    public CompletableFuture createToAll(String group, CreatePersistentSubscriptionToAllOptions options) {
        if (options == null) {
            options = CreatePersistentSubscriptionToAllOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new CreatePersistentSubscriptionToAll(this.client, group, options).execute();
    }

    public CompletableFuture createToStream(String stream, String group, CreatePersistentSubscriptionToStreamOptions options) {
        if (options == null) {
            options = CreatePersistentSubscriptionToStreamOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new CreatePersistentSubscriptionToStream(this.client, stream, group, options).execute();
    }

    /**
     * @deprecated prefer {@link #updateToStream(String, String)}
     */
    @Deprecated
    public CompletableFuture update(String stream, String group) {
        return this.updateToStream(stream, group);
    }

    public CompletableFuture updateToAll(String group) {
        return this.updateToAll(group, UpdatePersistentSubscriptionToAllOptions.get());
    }

    public CompletableFuture updateToStream(String stream, String group) {
        return this.updateToStream(stream, group, UpdatePersistentSubscriptionToStreamOptions.get());
    }

    /**
     * @deprecated prefer {@link #updateToStream(String, String, PersistentSubscriptionToStreamSettings)}
     */
    @Deprecated
    public CompletableFuture update(String stream, String group, PersistentSubscriptionSettings settings) {
        return this.updateToStream(stream, group, PersistentSubscriptionToStreamSettings.copy(settings).build());
    }

    public CompletableFuture updateToAll(String group, PersistentSubscriptionToAllSettings settings) {
        UpdatePersistentSubscriptionToAllOptions options = UpdatePersistentSubscriptionToAllOptions.get()
                .settings(settings);

        return this.updateToAll(group, options);
    }

    public CompletableFuture updateToStream(String stream, String group, PersistentSubscriptionToStreamSettings settings) {
        UpdatePersistentSubscriptionToStreamOptions options = UpdatePersistentSubscriptionToStreamOptions.get()
                .settings(settings);

        return this.updateToStream(stream, group, options);
    }

    /**
     * @deprecated prefer {@link #updateToStream(String, String, UpdatePersistentSubscriptionToStreamOptions)}
     */
    @Deprecated
    public CompletableFuture update(String stream, String group, UpdatePersistentSubscriptionOptions options) {
        if (options == null) {
            options = UpdatePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new UpdatePersistentSubscription(this.client, stream, group, options).execute();
    }

    public CompletableFuture updateToAll(String group, UpdatePersistentSubscriptionToAllOptions options) {
        if (options == null) {
            options = UpdatePersistentSubscriptionToAllOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new UpdatePersistentSubscriptionToAll(this.client, group, options).execute();
    }

    public CompletableFuture updateToStream(String stream, String group, UpdatePersistentSubscriptionToStreamOptions options) {
        if (options == null) {
            options = UpdatePersistentSubscriptionToStreamOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new UpdatePersistentSubscriptionToStream(this.client, stream, group, options).execute();
    }

    /**
     * @deprecated prefer {@link #deleteToStream(String, String)}
     */
    @Deprecated
    public CompletableFuture delete(String stream, String group) {
        return this.deleteToStream(stream, group);
    }

    public CompletableFuture deleteToAll(String group) {
        return this.deleteToAll(group, DeletePersistentSubscriptionOptions.get());
    }

    public CompletableFuture deleteToStream(String stream, String group) {
        return this.deleteToStream(stream, group, DeletePersistentSubscriptionOptions.get());
    }

    /**
     * @deprecated prefer {@link #deleteToStream(String, String, DeletePersistentSubscriptionOptions)}
     */
    @Deprecated
    public CompletableFuture delete(String stream, String group, DeletePersistentSubscriptionOptions options) {
        return this.deleteToStream(stream, group, options);
    }

    public CompletableFuture deleteToAll(String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new DeletePersistentSubscriptionToAll(this.client, group, options).execute();
    }

    public CompletableFuture deleteToStream(String stream, String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new DeletePersistentSubscriptionToStream(this.client, stream, group, options).execute();
    }

    /**
     * @deprecated prefer {@link #subscribeToStream(String, String, PersistentSubscriptionListener)}
     */
    @Deprecated
    public CompletableFuture subscribe(String stream, String group, PersistentSubscriptionListener listener) {
        return this.subscribeToStream(stream, group, listener);
    }

    public CompletableFuture subscribeToAll(String group, PersistentSubscriptionListener listener) {
        return this.subscribeToAll(group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    public CompletableFuture subscribeToStream(String stream, String group, PersistentSubscriptionListener listener) {
        return this.subscribeToStream(stream, group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    /**
     * @deprecated prefer {@link #subscribeToStream(String, String, SubscribePersistentSubscriptionOptions, PersistentSubscriptionListener)}
     */
    @Deprecated
    public CompletableFuture subscribe(String stream, String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        return this.subscribeToStream(stream, group, options, listener);
    }

    public CompletableFuture subscribeToAll(String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null) {
            options = SubscribePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new SubscribePersistentSubscriptionToAll(this.client, group, options, listener).execute();
    }

    public CompletableFuture subscribeToStream(String stream, String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null) {
            options = SubscribePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new SubscribePersistentSubscriptionToStream(this.client, stream, group, options, listener).execute();
    }
}

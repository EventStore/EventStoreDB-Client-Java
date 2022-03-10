package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EventStoreDBPersistentSubscriptionsClient extends EventStoreDBClientBase {
    private static final ObjectMapper mapper = new ObjectMapper();

    private EventStoreDBPersistentSubscriptionsClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    public static EventStoreDBPersistentSubscriptionsClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBPersistentSubscriptionsClient(settings);
    }

    public CompletableFuture create(String stream, String group) {
        return this.create(stream, group, CreatePersistentSubscriptionOptions.get());
    }

    public CompletableFuture createToAll(String group) {
        return this.createToAll(group, CreatePersistentSubscriptionToAllOptions.get());
    }

    public CompletableFuture create(String stream, String group, PersistentSubscriptionSettings settings) {
        CreatePersistentSubscriptionOptions options = CreatePersistentSubscriptionOptions.get()
                .settings(settings);

        return this.create(stream, group, options);
    }

    public CompletableFuture createToAll(String group, PersistentSubscriptionToAllSettings settings) {
        CreatePersistentSubscriptionToAllOptions options = CreatePersistentSubscriptionToAllOptions.get()
                .settings(settings);

        return this.createToAll(group, options);
    }

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

    public CompletableFuture update(String stream, String group) {
        return this.update(stream, group, UpdatePersistentSubscriptionOptions.get());
    }

    public CompletableFuture updateToAll(String group) {
        return this.updateToAll(group, UpdatePersistentSubscriptionToAllOptions.get());
    }

    public CompletableFuture update(String stream, String group, PersistentSubscriptionSettings settings) {
        UpdatePersistentSubscriptionOptions options = UpdatePersistentSubscriptionOptions.get()
                .settings(settings);

        return this.update(stream, group, options);
    }

    public CompletableFuture updateToAll(String group, PersistentSubscriptionToAllSettings settings) {
        UpdatePersistentSubscriptionToAllOptions options = UpdatePersistentSubscriptionToAllOptions.get()
                .settings(settings);

        return this.updateToAll(group, options);
    }

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

    public CompletableFuture delete(String stream, String group) {
        return this.delete(stream, group, DeletePersistentSubscriptionOptions.get());
    }

    public CompletableFuture deleteToAll(String group) {
        return this.deleteToAll(group, DeletePersistentSubscriptionOptions.get());
    }

    public CompletableFuture delete(String stream, String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new DeletePersistentSubscription(this.client, stream, group, options).execute();    }

    public CompletableFuture deleteToAll(String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new DeletePersistentSubscriptionToAll(this.client, group, options).execute();
    }

    public CompletableFuture<PersistentSubscription> subscribe(String stream, String group, PersistentSubscriptionListener listener) {
        return this.subscribe(stream, group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    public CompletableFuture<PersistentSubscription> subscribeToAll(String group, PersistentSubscriptionListener listener) {
        return this.subscribeToAll(group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    public CompletableFuture<PersistentSubscription> subscribe(String stream, String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null) {
            options = SubscribePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new SubscribePersistentSubscription(this.client, stream, group, options, listener).execute();    }

    public CompletableFuture<PersistentSubscription> subscribeToAll(String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null) {
            options = SubscribePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new SubscribePersistentSubscriptionToAll(this.client, group, options, listener).execute();
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listAll(ListPersistentSubscriptionsOptions options) {
        return ListPersistentSubscriptions.execute(this.client, options, "");
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listAll() {
        return listAll(ListPersistentSubscriptionsOptions.get());
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listForStream(String stream, ListPersistentSubscriptionsOptions options) {
        return ListPersistentSubscriptions.execute(this.client, options, stream);
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listForStream(String stream) {
        return listForStream(stream, ListPersistentSubscriptionsOptions.get());
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listToAll() {
        return listToAll(ListPersistentSubscriptionsOptions.get());
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listToAll(ListPersistentSubscriptionsOptions options) {
        return listForStream("$all", options);
    }

    public CompletableFuture<Optional<PersistentSubscriptionInfo>> getInfo(String stream, String groupName, GetPersistentSubscriptionInfoOptions options) {
        return GetPersistentSubscriptionInfo.execute(this.client, options, stream, groupName);
    }

    public CompletableFuture<Optional<PersistentSubscriptionInfo>> getInfo(String stream, String groupName) {
        return getInfo(stream, groupName, GetPersistentSubscriptionInfoOptions.get());
    }

    public CompletableFuture<Optional<PersistentSubscriptionInfo>> getInfoToAll(String groupName, GetPersistentSubscriptionInfoOptions options) {
        return getInfo("$all", groupName, options);
    }

    public CompletableFuture<Optional<PersistentSubscriptionInfo>> getInfoToAll(String groupName) {
        return getInfoToAll(groupName, GetPersistentSubscriptionInfoOptions.get());
    }

    public CompletableFuture replayParkedMessages(String stream, String groupName, ReplayParkedMessagesOptions options) {
        return ReplayParkedMessages.execute(this.client, options, stream, groupName);
    }

    public CompletableFuture replayParkedMessages(String stream, String groupName) {
        return replayParkedMessages(stream, groupName, ReplayParkedMessagesOptions.get());
    }

    public CompletableFuture replayParkedMessagesToAll(String groupName, ReplayParkedMessagesOptions options) {
        return replayParkedMessages("$all", groupName, options);
    }

    public CompletableFuture replayParkedMessagesToAll(String groupName) throws ExecutionException, InterruptedException {
        return replayParkedMessagesToAll(groupName, ReplayParkedMessagesOptions.get());
    }

    public CompletableFuture restartSubsystem() {
        return restartSubsystem(RestartPersistentSubscriptionSubsystemOptions.get());
    }

    public CompletableFuture restartSubsystem(RestartPersistentSubscriptionSubsystemOptions options) {
        return RestartPersistentSubscriptionSubsystem.execute(this.client, options);
    }
}

package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class EventStoreDBPersistentSubscriptionsClient extends EventStoreDBClientBase {
    private static final ObjectMapper mapper = new ObjectMapper();

    private EventStoreDBPersistentSubscriptionsClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    public static EventStoreDBPersistentSubscriptionsClient createToStream(EventStoreDBClientSettings settings) {
        return new EventStoreDBPersistentSubscriptionsClient(settings);
    }

    public CompletableFuture createToStream(String stream, String group) {
        return this.createToStream(stream, group, CreatePersistentSubscriptionToStreamOptions.get());
    }

    public CompletableFuture createToAll(String group) {
        return this.createToAll(group, CreatePersistentSubscriptionToAllOptions.get());
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

    public CompletableFuture createToAll(String group, CreatePersistentSubscriptionToAllOptions options) {
        if (options == null) {
            options = CreatePersistentSubscriptionToAllOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new CreatePersistentSubscriptionToAll(this.client, group, options).execute();
    }

    public CompletableFuture updateToStream(String stream, String group) {
        return this.updateToStream(stream, group, UpdatePersistentSubscriptionToStreamOptions.get());
    }

    public CompletableFuture updateToAll(String group) {
        return this.updateToAll(group, UpdatePersistentSubscriptionToAllOptions.get());
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

    public CompletableFuture updateToAll(String group, UpdatePersistentSubscriptionToAllOptions options) {
        if (options == null) {
            options = UpdatePersistentSubscriptionToAllOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new UpdatePersistentSubscriptionToAll(this.client, group, options).execute();
    }

    public CompletableFuture deleteToStream(String stream, String group) {
        return this.deleteToStream(stream, group, DeletePersistentSubscriptionOptions.get());
    }

    public CompletableFuture deleteToAll(String group) {
        return this.deleteToAll(group, DeletePersistentSubscriptionOptions.get());
    }

    public CompletableFuture deleteToStream(String stream, String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new DeletePersistentSubscriptionToStream(this.client, stream, group, options).execute();    }

    public CompletableFuture deleteToAll(String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new DeletePersistentSubscriptionToAll(this.client, group, options).execute();
    }

    public CompletableFuture<PersistentSubscription> subscribeToStream(String stream, String group, PersistentSubscriptionListener listener) {
        return this.subscribeToStream(stream, group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    public CompletableFuture<PersistentSubscription> subscribeToAll(String group, PersistentSubscriptionListener listener) {
        return this.subscribeToAll(group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    public CompletableFuture<PersistentSubscription> subscribeToStream(String stream, String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null) {
            options = SubscribePersistentSubscriptionOptions.get();
        }

        if (!options.hasUserCredentials()) {
            options.authenticated(this.credentials);
        }

        return new SubscribePersistentSubscriptionToStream(this.client, stream, group, options, listener).execute();    }

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
        return ListPersistentSubscriptions.execute(this.client, options, "", Function.identity());
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listAll() {
        return listAll(ListPersistentSubscriptionsOptions.get());
    }

    public CompletableFuture<List<PersistentSubscriptionToStreamInfo>> listToStream(String stream, ListPersistentSubscriptionsOptions options) {
        return ListPersistentSubscriptions.execute(this.client, options, stream, info -> (PersistentSubscriptionToStreamInfo) info);
    }

    public CompletableFuture<List<PersistentSubscriptionToStreamInfo>> listToStream(String stream) {
        return listToStream(stream, ListPersistentSubscriptionsOptions.get());
    }

    public CompletableFuture<List<PersistentSubscriptionToAllInfo>> listToAll() {
        return listToAll(ListPersistentSubscriptionsOptions.get());
    }

    public CompletableFuture<List<PersistentSubscriptionToAllInfo>> listToAll(ListPersistentSubscriptionsOptions options) {
        return ListPersistentSubscriptions.execute(this.client, options, "$all", info -> (PersistentSubscriptionToAllInfo) info);
    }

    public CompletableFuture<Optional<PersistentSubscriptionInfo>> getInfoToStream(String stream, String groupName, GetPersistentSubscriptionInfoOptions options) {
        return GetPersistentSubscriptionInfo.execute(this.client, options, stream, groupName);
    }

    public CompletableFuture<Optional<PersistentSubscriptionInfo>> getInfoToStream(String stream, String groupName) {
        return getInfoToStream(stream, groupName, GetPersistentSubscriptionInfoOptions.get());
    }

    public CompletableFuture<Optional<PersistentSubscriptionInfo>> getInfoToAll(String groupName, GetPersistentSubscriptionInfoOptions options) {
        return getInfoToStream("$all", groupName, options);
    }

    public CompletableFuture<Optional<PersistentSubscriptionInfo>> getInfoToAll(String groupName) {
        return getInfoToAll(groupName, GetPersistentSubscriptionInfoOptions.get());
    }

    public CompletableFuture replayParkedMessagesToStream(String stream, String groupName, ReplayParkedMessagesOptions options) {
        return ReplayParkedMessages.execute(this.client, options, stream, groupName);
    }

    public CompletableFuture replayParkedMessagesToStream(String stream, String groupName) {
        return replayParkedMessagesToStream(stream, groupName, ReplayParkedMessagesOptions.get());
    }

    public CompletableFuture replayParkedMessagesToAll(String groupName, ReplayParkedMessagesOptions options) {
        return replayParkedMessagesToStream("$all", groupName, options);
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

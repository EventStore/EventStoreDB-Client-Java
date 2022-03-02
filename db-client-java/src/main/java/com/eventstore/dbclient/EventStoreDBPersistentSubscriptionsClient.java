package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        return getHttpURLConnection(options, "/subscriptions").thenApply(http -> {
            try {
                http.setRequestMethod("GET");

                throwOnError(http.getResponseCode());

                String content = readContent(http);
                List<PersistentSubscriptionInfo> ps = new ArrayList<>();

                for (JsonNode node: mapper.readTree(content)) {
                    ps.add(parseSubscriptionInfo(node));
                }
                return ps;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                http.disconnect();
            }
        });
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listAll() {
        return listAll(ListPersistentSubscriptionsOptions.get());
    }

    public CompletableFuture<List<PersistentSubscriptionInfo>> listForStream(String stream, ListPersistentSubscriptionsOptions options) {
        return getHttpURLConnection(options, "/subscriptions/" + urlEncode(stream)).thenApply(http -> {
            try {
                http.setRequestMethod("GET");

                throwOnError(http.getResponseCode());

                String content = readContent(http);
                List<PersistentSubscriptionInfo> ps = new ArrayList<>();

                for (JsonNode node: mapper.readTree(content)) {
                    ps.add(parseSubscriptionInfo(node));
                }
                return ps;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                http.disconnect();
            }
        });
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
        return getHttpURLConnection(options, "/subscriptions/" + urlEncode(stream) + "/" + urlEncode(groupName) + "/info").thenApply(http -> {
            try {
                http.setRequestMethod("GET");
                int code = http.getResponseCode();

                if (code == 404)
                    return Optional.empty();

                throwOnError(code);

                String content = readContent(http);
                JsonNode node = mapper.readTree(content);

                return Optional.of(parseSubscriptionInfo(node));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                http.disconnect();
            }
        });
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
        String query;

        if (options.getStopAt() != null) {
            query = "?stopAt=" + options.getStopAt();
        } else {
            query = "";
        }

        return getHttpURLConnection(options, "/subscriptions/" + urlEncode(stream) + "/" + urlEncode(groupName) + "/replayParked" + query).thenApply(http -> {
            try {
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                http.setFixedLengthStreamingMode(0);

                throwOnError(http.getResponseCode());
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                http.disconnect();
            }
        });
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
        return restartSubsystem(RestartPersistentSubscriptionSubsystem.get());
    }

    public CompletableFuture restartSubsystem(RestartPersistentSubscriptionSubsystem options) {
        return getHttpURLConnection(options, "/subscriptions/restart").thenApply(http -> {
            try {
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                http.setFixedLengthStreamingMode(0);

                throwOnError(http.getResponseCode());
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                http.disconnect();
            }
        });
    }

    private <A> CompletableFuture<HttpURLConnection> getHttpURLConnection(OptionsBase<A> options, String path) {
        return this.client.getCurrentEndpoint().thenApply(edp -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) edp.getURL(this.client.settings.isTls(), path).openConnection();
                conn.setRequestProperty("Accept", "application/json");
                String creds = options.getUserCredentials();

                if (creds == null && this.client.settings.getDefaultCredentials() != null) {
                    creds = this.client.settings.getDefaultCredentials().toUserCredentials().basicAuthHeader();
                }

                if (creds != null) {
                    conn.setRequestProperty("Authorization", creds);
                }

                return conn;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String readContent(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = in.readLine()) != null) {
                content.append(line);
            }

            return content.toString();
        }
    }

    private static PersistentSubscriptionInfo parseSubscriptionInfo(JsonNode node) {
        PersistentSubscriptionInfo info = new PersistentSubscriptionInfo();

        info.setEventStreamId(node.get("eventStreamId").asText());
        info.setGroupName(node.get("groupName").asText());
        info.setStatus(node.get("status").asText());
        info.setAverageItemsPerSecond(node.get("averageItemsPerSecond").asDouble());
        info.setTotalItemsProcessed(node.get("totalItemsProcessed").asLong());
        info.setLastKnownEventNumber(node.get("lastKnownEventNumber").asLong());

        if (node.get("connectionCount") != null) {
            info.setConnectionCount(node.get("connectionCount").asLong());
        } else {
            info.setConnectionCount(0);
        }

        info.setTotalInFlightMessages(node.get("totalInFlightMessages").asLong());
        info.setConfig(parseConfig(node.get("config")));

        return info;
    }

    private static PersistentSubscriptionConfig parseConfig(JsonNode node) {
        PersistentSubscriptionConfig config = null;

        if (node != null) {
            config = new PersistentSubscriptionConfig();
            config.setResolveLinktos(node.get("resolveLinktos").asBoolean());
            config.setStartFrom(node.get("startFrom").asLong());
            config.setMessageTimeoutMilliseconds(node.get("messageTimeoutMilliseconds").asLong());
            config.setExtraStatistics(node.get("extraStatistics").asBoolean());
            config.setMaxRetryCount(node.get("maxRetryCount").asLong());
            config.setLiveBufferSize(node.get("liveBufferSize").asLong());
            config.setBufferSize(node.get("bufferSize").asLong());
            config.setReadBatchSize(node.get("readBatchSize").asLong());
            config.setPreferRoundRobin(node.get("preferRoundRobin").asBoolean());
            config.setCheckPointAfterMilliseconds(node.get("checkPointAfterMilliseconds").asLong());
            config.setMinCheckPointCount(node.get("minCheckPointCount").asLong());
            config.setMaxCheckPointCount(node.get("maxCheckPointCount").asLong());
            config.setMaxSubscriberCount(node.get("maxSubscriberCount").asLong());
            config.setNamedConsumerStrategy(ConsumerStrategy.valueOf(node.get("namedConsumerStrategy").asText()));
        }

        return config;
    }

    private static void throwOnError(int code) {
        switch (code) {
            case 401:
                throw new RuntimeException("Access denied");
            case 404:
                throw new ResourceNotFoundException();
            default:
                if (code >= 200 && code < 300)
                    return;

                throw new RuntimeException("Unexpected exception, code: " + code);
        }
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.eventstore.dbclient;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class EventStoreDBClient {
    final private GrpcClient client;
    final private UserCredentials credentials;

    private EventStoreDBClient(EventStoreDBClientSettings settings) {
        ConnectionBuilder builder = new ConnectionBuilder();
        if (settings.getDefaultCredentials() != null) {
            this.credentials = settings.getDefaultCredentials().toUserCredentials();
        } else {
            this.credentials = null;
        }

        if (settings.isTls()) {
            try {
                SslContextBuilder sslContext = GrpcSslContexts.forClient();

                if (!settings.isTlsVerifyCert()) {
                    sslContext.trustManager(InsecureTrustManagerFactory.INSTANCE);
                }

                builder.sslContext(sslContext.build());
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }
        }

        if (settings.isDnsDiscover()) {
            this.client = builder.createClusterConnectionUsingDns(settings.getHosts()[0], settings.getNodePreference());
            return;
        }

        if (settings.getHosts().length > 1) {
            this.client = builder.createClusterConnectionUsingSeeds(
                    settings.getHosts(),
                    settings.getNodePreference()
            );
            return;
        }

        this.client = builder.createSingleNodeConnection(settings.getHosts()[0]);
    }

    public static EventStoreDBClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBClient(settings);
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, EventData... events) {
        return this.appendToStream(streamName, Arrays.stream(events).iterator());
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, Iterator<EventData> events) {
        return this.appendToStream(streamName, AppendToStreamOptions.get(), events);
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

    public CompletableFuture<ReadResult> readStream(String streamName) {
        return this.readStream(streamName, Long.MAX_VALUE, ReadStreamOptions.get());
    }

    public CompletableFuture<ReadResult> readStream(String streamName, long maxCount) {
        return this.readStream(streamName, maxCount, ReadStreamOptions.get());
    }

    public CompletableFuture<ReadResult> readStream(String streamName, ReadStreamOptions options) {
        return this.readStream(streamName, Long.MAX_VALUE, ReadStreamOptions.get());
    }

    public CompletableFuture<ReadResult> readStream(String streamName, long maxCount, ReadStreamOptions options) {
        if (options == null)
            options = ReadStreamOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new ReadStream(this.client, streamName, maxCount, options).execute();
    }

    public CompletableFuture<ReadResult> readAll() {
        return this.readAll(Long.MAX_VALUE, ReadAllOptions.get());
    }

    public CompletableFuture<ReadResult> readAll(long maxCount) {
        return this.readAll(maxCount, ReadAllOptions.get());
    }

    public CompletableFuture<ReadResult> readAll(ReadAllOptions options) {
        return this.readAll(Long.MAX_VALUE, options);
    }

    public CompletableFuture<ReadResult> readAll(long maxCount, ReadAllOptions options) {
        if (options == null)
            options = ReadAllOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new ReadAll(this.client, maxCount, options).execute();
    }

    public CompletableFuture<Subscription> subscribeToStream(String streamName, SubscriptionListener listener) {
        return this.subscribeToStream(streamName, listener, SubscribeToStreamOptions.get());
    }

    public CompletableFuture<Subscription> subscribeToStream(String streamName, SubscriptionListener listener, SubscribeToStreamOptions options) {
        if (options == null)
            options = SubscribeToStreamOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new SubscribeToStream(this.client, streamName, listener, options).execute();
    }

    public CompletableFuture<Subscription> subscribeToAll(SubscriptionListener listener) {
        return this.subscribeToAll(listener, SubscribeToAllOptions.get());
    }

    public CompletableFuture<Subscription> subscribeToAll(SubscriptionListener listener, SubscribeToAllOptions options) {
        if (options == null)
            options = SubscribeToAllOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new SubscribeToAll(this.client, listener, options).execute();
    }

    public CompletableFuture<DeleteResult> deleteStream(String streamName) {
        return this.deleteStream(streamName, DeleteStreamOptions.get());
    }

    public CompletableFuture<DeleteResult> deleteStream(String streamName, DeleteStreamOptions options) {
        if (options == null)
            options = DeleteStreamOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new DeleteStream(this.client, streamName, options).execute();
    }

    public PersistentSubscriptions persistentSubscriptions() {
        return new PersistentSubscriptions(this.client, this.credentials);
    }

    public void shutdown() throws InterruptedException {
        this.client.shutdown();
    }

    public Projections projections() {
        return new Projections(this.client, this.credentials);
    }
}

package com.eventstore.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

abstract class GrpcClient {
    protected final EventStoreDBClientSettings settings;
    protected final SslContext sslContext;
    private final Logger logger = LoggerFactory.getLogger(GrpcClient.class);
    private final LinkedBlockingQueue<Msg> messages;
    protected ManagedChannel channel;
    protected Endpoint endpoint;
    protected Exception lastException;
    protected UUID currentChannelId;
    protected Optional<ServerInfo> serverInfo = Optional.empty();

    protected volatile boolean shutdownRequested = false;
    protected volatile boolean shutdown = false;

    protected GrpcClient(EventStoreDBClientSettings settings, SslContext sslContext) {
        this.settings = settings;
        this.sslContext = sslContext;
        this.messages = new LinkedBlockingQueue<>();
        this.currentChannelId = UUID.randomUUID();
    }

    protected void startConnectionLoop() {
        pushMsg(new CreateChannel(this.currentChannelId));
        CompletableFuture.runAsync(this::messageLoop, createConnectionLoopExecutor());
    }

    protected Executor createConnectionLoopExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "esdb-client-" + currentChannelId);
            thread.setDaemon(true);
            return thread;
        });
    }

    protected abstract boolean doConnect();

    protected void pushMsg(Msg msg) {
        try {
            if (shutdown) {
                if (msg instanceof RunWorkItem) {
                    RunWorkItem args = (RunWorkItem) msg;
                    args.reportError(new ConnectionShutdownException());
                }

                if (msg instanceof Shutdown) {
                    ((Shutdown) msg).completed.accept(42);
                }

                return;
            }

            this.messages.put(msg);
        } catch (InterruptedException e) {
            logger.error("Unexpected exception occurred when pushing a new message", e);
        }
    }

    public EventStoreDBClientSettings getSettings() {
        return settings;
    }

    public <A> CompletableFuture<A> runWithArgs(Function<WorkItemArgs, CompletableFuture<A>> action) {
        final CompletableFuture<A> result = new CompletableFuture<>();
        final GrpcClient self = this;

        this.pushMsg(new RunWorkItem((args, fatalError) -> {
            if (fatalError != null) {
                result.completeExceptionally(fatalError);
                return;
            }

            action.apply(args).whenComplete((outcome, error) -> {
                if (outcome != null) {
                    result.complete(outcome);
                    return;
                }

                if (error instanceof NotLeaderException) {
                    NotLeaderException ex = (NotLeaderException) error;
                    // TODO - Currently we don't retry on not leader exception but we might consider
                    // allowing this on a case-by-case basis.
                    result.completeExceptionally(ex);
                    self.pushMsg(new CreateChannel(args.id, ex.getLeaderEndpoint()));

                    return;
                }

                if (error instanceof StatusRuntimeException) {
                    StatusRuntimeException ex = (StatusRuntimeException) error;

                    if (ex.getStatus().getCode().equals(Status.Code.UNAVAILABLE) || ex.getStatus().getCode().equals(Status.Code.ABORTED)) {
                        self.pushMsg(new CreateChannel(args.id));
                    }
                }

                result.completeExceptionally(error);
            });
        }));

        return result;
    }

    public <A> CompletableFuture<A> run(Function<ManagedChannel, CompletableFuture<A>> action) {
        return runWithArgs(args -> action.apply(args.getChannel()));
    }

    public CompletableFuture<Endpoint> getCurrentEndpoint() {
        return runWithArgs(args -> CompletableFuture.completedFuture(args.endpoint));
    }

    private boolean discover(UUID previousId, Optional<Endpoint> candidate) {
        long attempts = 1;

        // It means we already created a new channel and it was old request.
        if (!currentChannelId.equals(previousId))
            return true;

        if (candidate.isPresent()) {
            shutdownPreviousChannelIfExists();
            this.endpoint = candidate.get();
            this.channel = createChannel(this.endpoint);

            try {
                if (loadServerFeatures()) {
                    this.currentChannelId = UUID.randomUUID();
                    return true;
                }
            } catch (Exception e) {
                logger.error("A fatal exception happened when fetching server supported features", e);
            }
        }

        for (; ; ) {
            logger.debug("Start connection attempt ({}/{})", attempts, settings.getMaxDiscoverAttempts());
            shutdownPreviousChannelIfExists();
            if (doConnect()) {
                try {
                    if (loadServerFeatures()) {
                        currentChannelId = UUID.randomUUID();
                        logger.info("Connection created successfully");
                        return true;
                    }
                } catch (Exception e) {
                    logger.error("A fatal exception happened when fetching server supported features", e);
                    return false;
                }
            }

            ++attempts;
            if (attempts > settings.getMaxDiscoverAttempts()) {
                logger.error("Maximum discovery attempt count reached: {}", settings.getMaxDiscoverAttempts());
                return false;
            }

            logger.warn("Unable to find a node. Retrying... ({}/{})", attempts, settings.getMaxDiscoverAttempts());
            sleep(settings.getDiscoveryInterval());
        }
    }

    private void shutdownPreviousChannelIfExists() {
        if (this.channel != null && !this.channel.isShutdown()) {
            try {
                this.channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Shutdown of existing channel has been interrupted.", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean loadServerFeatures() {
        try {
            serverInfo = ServerFeatures.getSupportedFeatures(settings, channel);
            return true;
        } catch (ServerFeatures.RetryableException e) {
            logger.warn("An exception happened when fetching server supported features. Retrying connection attempt.", e);
            return false;
        }
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error("Thread is interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private boolean handleMsg(Msg msg) {
        boolean result = false;

        if (msg instanceof CreateChannel) {
            if (!this.shutdown) {
                CreateChannel args = (CreateChannel) msg;
                result = discover(args.previousId, args.channel);
            } else {
                logger.warn("Channel creation request ignored, the connection is already closed");
            }
        } else if (msg instanceof RunWorkItem) {
            RunWorkItem args = (RunWorkItem) msg;

            if (this.shutdown) {
                Exception e = this.lastException != null ? this.lastException : new ConnectionShutdownException();

                logger.warn("Receive an command request but the connection is already closed", e);
                args.item.accept(null, e);
            } else {
                // In case if the channel hasn't been resolved yet.
                if (this.channel == null) {
                    try {
                        this.messages.put(new RunWorkItem(args.item));
                        logger.debug("Channel is not resolved yet, parking current work item");
                    } catch (InterruptedException e) {
                        logger.error("Exception occurred when parking a work item", e);

                        args.item.accept(null, e);
                    }
                } else {
                    WorkItemArgs workItemArgs = new WorkItemArgs(this.currentChannelId, this.channel, this.endpoint, this.serverInfo);
                    args.item.accept(workItemArgs, null);
                }
            }

            result = true;
        } else if (msg instanceof Shutdown) {
            if (!this.shutdown) {
                logger.info("Received a shutdown request, closing...");
                closeConnection();
                result = false;
                logger.info("Connection was closed successfully");
            } else {
                ((Shutdown) msg).completed.accept(42);
                logger.info("Shutdown request ignored, connection is already closed");
            }
        }

        return result;
    }

    private void messageLoop() {
        Consumer<Object> shutdownCompleted = null;
        for (; ; ) {
            try {
                Msg msg = this.messages.take();
                logger.debug("Current msg: {}", msg);

                if (!handleMsg(msg)) {
                    this.shutdown = true;

                    if (msg instanceof Shutdown) {
                        shutdownCompleted = ((Shutdown) msg).completed;
                    }

                    break;
                }
            } catch (InterruptedException e) {
                this.lastException = e;
                this.shutdown = true;
                break;
            }
        }

        logger.debug("Draining pending requests...");
        ArrayList<Msg> msgs = new ArrayList<>();
        this.messages.drainTo(msgs);

        for (Msg msg : msgs) {
            //noinspection ResultOfMethodCallIgnored
            handleMsg(msg);
        }

        if (shutdownCompleted != null)
            shutdownCompleted.accept(42);

        logger.debug("Drainage completed successfully");
    }

    private void closeConnection() {
        if (this.channel != null) {
            try {
                this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Error when closing gRPC channel", e);
            } finally {
                this.channel = null;
            }
        }
    }

    protected ManagedChannel createChannel(Endpoint endpoint) {
        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(endpoint.getHostname(), endpoint.getPort());

        if (this.sslContext == null) {
            builder.usePlaintext();
        } else {
            builder.sslContext(this.sslContext);
        }

        if (settings.getKeepAliveTimeout() <= 0)
            builder.keepAliveTimeout(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        else
            builder.keepAliveTimeout(settings.getKeepAliveTimeout(), TimeUnit.MILLISECONDS);

        if (settings.getKeepAliveInterval() <= 0)
            builder.keepAliveTime(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        else
            builder.keepAliveTime(settings.getKeepAliveInterval(), TimeUnit.MILLISECONDS);

        return builder.build();
    }

    public void shutdown() throws ExecutionException, InterruptedException {
        shutdownRequested = true;
        final CompletableFuture<Object> completion = new CompletableFuture<>();

        pushMsg(new Shutdown(completion::complete));

        completion.get();
    }

    public boolean isShutdown() {
        return shutdown || shutdownRequested;
    }

    class WorkItemArgs {
        private final UUID id;
        private final ManagedChannel channel;
        private final Endpoint endpoint;
        private final Optional<ServerInfo> info;

        public WorkItemArgs(UUID id, ManagedChannel channel, Endpoint endpoint, Optional<ServerInfo> info) {
            this.id = id;
            this.channel = channel;
            this.endpoint = endpoint;
            this.info = info;
        }

        public UUID getId() {
            return id;
        }

        public ManagedChannel getChannel() {
            return channel;
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        public boolean supportFeature(int feature) {
            if (info.isPresent()) {
                return info.get().supportFeature(feature);
            }

            return false;
        }

        public <A> HttpURLConnection getHttpConnection(OptionsBase<A> options, EventStoreDBClientSettings settings, String path) {
            try {
                HttpURLConnection conn = (HttpURLConnection) this.endpoint.getURL(settings.isTls(), path).openConnection();
                conn.setRequestProperty("Accept", "application/json");
                String creds = options.getUserCredentials();

                if (creds == null && settings.getDefaultCredentials() != null) {
                    creds = settings.getDefaultCredentials().toUserCredentials().basicAuthHeader();
                }

                if (creds != null) {
                    conn.setRequestProperty("Authorization", creds);
                }

                return conn;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FunctionalInterface
    interface WorkItem {
        void accept(WorkItemArgs args, Exception error);
    }

    private interface Msg {
    }

    class CreateChannel implements Msg {
        final Optional<Endpoint> channel;
        final UUID previousId;

        CreateChannel(UUID previousId) {
            this.channel = Optional.empty();
            this.previousId = previousId;
        }

        CreateChannel(UUID previousId, Endpoint endpoint) {
            this.channel = Optional.of(endpoint);
            this.previousId = previousId;
        }
    }

    class RunWorkItem implements Msg {
        final WorkItem item;

        RunWorkItem(WorkItem item) {
            this.item = item;
        }

        void reportError(Exception e) {
            this.item.accept(null, e);
        }
    }

    class Shutdown implements Msg {
        final Consumer<Object> completed;

        Shutdown(Consumer<Object> completed) {
            this.completed = completed;
        }
    }
}

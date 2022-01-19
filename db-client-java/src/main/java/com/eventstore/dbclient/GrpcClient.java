package com.eventstore.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public abstract class GrpcClient {
    protected final EventStoreDBClientSettings settings;
    protected final SslContext sslContext;
    private final Logger logger = LoggerFactory.getLogger(GrpcClient.class);
    private final LinkedBlockingQueue<Msg> messages;
    protected ManagedChannel channel;
    protected Exception lastException;
    protected UUID currentChannelId;

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

    public <A> CompletableFuture<A> run(Function<ManagedChannel, CompletableFuture<A>> action) {
        final CompletableFuture<A> result = new CompletableFuture<>();
        final GrpcClient self = this;

        this.pushMsg(new RunWorkItem((id, channel, fatalError) -> {
            if (fatalError != null) {
                result.completeExceptionally(fatalError);
                return;
            }

            action.apply(channel).whenComplete((outcome, error) -> {
                if (outcome != null) {
                    result.complete(outcome);
                    return;
                }

                if (error instanceof NotLeaderException) {
                    NotLeaderException ex = (NotLeaderException) error;
                    // TODO - Currently we don't retry on not leader exception but we might consider
                    // allowing this on a case-by-case basis.
                    result.completeExceptionally(ex);
                    self.pushMsg(new CreateChannel(id, ex.getLeaderEndpoint()));

                    return;
                }

                if (error instanceof StatusRuntimeException) {
                    StatusRuntimeException ex = (StatusRuntimeException) error;

                    if (ex.getStatus().getCode().equals(Status.Code.UNAVAILABLE) || ex.getStatus().getCode().equals(Status.Code.ABORTED)) {
                        self.pushMsg(new CreateChannel(id));
                    }
                }

                result.completeExceptionally(error);
            });
        }));

        return result;
    }

    private boolean discover(UUID previousId, Optional<Endpoint> candidate) {
        long attempts = 1;

        // It means we already created a new channel and it was old request.
        if (!currentChannelId.equals(previousId))
            return true;

        if (candidate.isPresent()) {
            this.channel = createChannel(candidate.get());
            this.currentChannelId = UUID.randomUUID();

            return true;
        }

        for (; ; ) {
            logger.debug("Start connection attempt ({}/{})", attempts, settings.getMaxDiscoverAttempts());
            if (doConnect()) {
                currentChannelId = UUID.randomUUID();

                logger.info("Connection created successfully");

                return true;
            } else {
                ++attempts;
                if (attempts > settings.getMaxDiscoverAttempts()) {
                    logger.error("Maximum discovery attempt count reached: {}", settings.getMaxDiscoverAttempts());
                    return false;
                }

                logger.warn("Unable to find a node. Retrying... ({}/{})", attempts, settings.getMaxDiscoverAttempts());
                sleep(settings.getDiscoveryInterval());
            }
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
                args.item.accept(null, null, e);
            } else {
                // In case if the channel hasn't been resolved yet.
                if (this.channel == null) {
                    try {
                        this.messages.put(new RunWorkItem(args.item));
                        logger.debug("Channel is not resolved yet, parking current work item");
                    } catch (InterruptedException e) {
                        logger.error("Exception occurred when parking a work item", e);

                        args.item.accept(null, null, e);
                    }
                } else {
                    args.item.accept(this.currentChannelId, this.channel, null);
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
                this.channel.shutdown().awaitTermination(Timeouts.DEFAULT.shutdownTimeout, Timeouts.DEFAULT.shutdownTimeoutUnit);
            } catch (InterruptedException e) {
                logger.error("Error when closing gRPC channel", e);
            } finally {
                this.channel = null;
            }
        }
    }

    protected ManagedChannel createChannel(Endpoint endpoint) {
        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(endpoint.getHostname(), endpoint.getPort())
                .maxInboundMessageSize(16 * 1024 * 1024)
                .maxInboundMetadataSize(16 * 1024 * 1024)
                .flowControlWindow(16 * 1024 * 1024)
                .initialFlowControlWindow(16 * 1024 * 1024)
                .perRpcBufferLimit(16 * 1024 * 1024);

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
        final CompletableFuture<Object> completion = new CompletableFuture<>();

        pushMsg(new Shutdown(completion::complete));

        completion.get();
    }

    @FunctionalInterface
    interface WorkItem {
        void accept(UUID id, ManagedChannel channel, Exception error);
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
            this.item.accept(null, null, e);
        }
    }

    class Shutdown implements Msg {
        final Consumer<Object> completed;

        Shutdown(Consumer<Object> completed) {
            this.completed = completed;
        }
    }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsGrpc;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;

abstract class AbstractRegularSubscription {
    protected static final StreamsOuterClass.ReadReq.Options.Builder defaultReadOptions;
    protected static final StreamsOuterClass.ReadReq.Options.Builder defaultSubscribeOptions;

    protected SubscriptionListener listener;
    protected Checkpointer checkpointer = null;
    private final GrpcClient client;
    private final OptionsBase options;

    protected AbstractRegularSubscription(GrpcClient client, OptionsBase options) {
        this.client = client;
        this.options = options;
    }

    static {
        defaultReadOptions = StreamsOuterClass.ReadReq.Options.newBuilder()
                .setUuidOption(StreamsOuterClass.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
        defaultSubscribeOptions = defaultReadOptions.clone()
                .setReadDirection(StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards)
                .setSubscription(StreamsOuterClass.ReadReq.Options.SubscriptionOptions.getDefaultInstance());
    }

    protected abstract StreamsOuterClass.ReadReq.Options.Builder createOptions();

    @SuppressWarnings("unchecked")
    public CompletableFuture<Subscription> execute() {
        return this.client.run(channel -> {
            StreamsOuterClass.ReadReq readReq = StreamsOuterClass.ReadReq.newBuilder()
                    .setOptions(createOptions())
                    .build();

            StreamsGrpc.StreamsStub client = GrpcUtils.configureStub(StreamsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<Subscription> future = new CompletableFuture<>();
            ClientResponseObserver<StreamsOuterClass.ReadReq, StreamsOuterClass.ReadResp> observer = new ClientResponseObserver<StreamsOuterClass.ReadReq, StreamsOuterClass.ReadResp>() {
                private boolean _confirmed;
                private Subscription _subscription;
                private ClientCallStreamObserver<StreamsOuterClass.ReadReq> _requestStream;

                @Override
                public void beforeStart(ClientCallStreamObserver<StreamsOuterClass.ReadReq> requestStream) {
                    this._requestStream = requestStream;
                }

                @Override
                public void onNext(@NotNull StreamsOuterClass.ReadResp readResp) {
                    if (!_confirmed && readResp.hasConfirmation()) {
                        this._confirmed = true;
                        this._subscription = new Subscription(this._requestStream,
                                readResp.getConfirmation().getSubscriptionId(), checkpointer);
                        future.complete(this._subscription);
                        listener.onConfirmation(this._subscription);
                        return;
                    }

                    if (!_confirmed && readResp.hasEvent()) {
                        onError(new IllegalStateException("Unconfirmed subscription received event"));
                        return;
                    }

                    if (_confirmed && readResp.hasCheckpoint()) {
                        Checkpointer checkpointer = this._subscription.getCheckpointer();
                        if (checkpointer == null) {
                            return;
                        }

                        StreamsOuterClass.ReadResp.Checkpoint checkpoint = readResp.getCheckpoint();
                        Position checkpointPos = new Position(checkpoint.getCommitPosition(), checkpoint.getPreparePosition());
                        checkpointer.onCheckpoint(this._subscription, checkpointPos);
                        return;
                    }

                    if (_confirmed && !readResp.hasEvent()) {
                        onError(new IllegalStateException(
                                String.format("Confirmed subscription %s received non-{event,checkpoint} variant",
                                        _subscription.getSubscriptionId())));
                        return;
                    }

                    listener.onEvent(this._subscription, ResolvedEvent.fromWire(readResp.getEvent()));
                }

                @Override
                public void onError(Throwable throwable) {
                    if (!_confirmed) {
                        future.completeExceptionally(throwable);
                    }

                    Throwable error = throwable;
                    if (error instanceof StatusRuntimeException) {
                        StatusRuntimeException sre = (StatusRuntimeException) error;
                        if (sre.getStatus().getCode() == Status.Code.CANCELLED) {
                            listener.onCancelled(this._subscription);
                            return;
                        }

                        String leaderHost = sre.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                        String leaderPort = sre.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                        if (leaderHost != null && leaderPort != null) {
                            error = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                        }
                    }

                    listener.onError(this._subscription, error);
                }

                @Override
                public void onCompleted() {
                    // Subscriptions should only complete on error.
                }
            };

            client.read(readReq, observer);

            return future;
        });
    }
}

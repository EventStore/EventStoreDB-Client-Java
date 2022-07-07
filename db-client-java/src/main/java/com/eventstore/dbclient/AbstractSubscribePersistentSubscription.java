package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;

abstract class AbstractSubscribePersistentSubscription {
    protected static final Persistent.ReadReq.Options.Builder defaultReadOptions;
    private final GrpcClient connection;
    private final String group;
    private final PersistentSubscriptionListener listener;
    private final SubscribePersistentSubscriptionOptions options;

    static {
        defaultReadOptions = Persistent.ReadReq.Options.newBuilder()
                .setUuidOption(Persistent.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
    }

    public AbstractSubscribePersistentSubscription(GrpcClient connection, String group,
                                                   SubscribePersistentSubscriptionOptions options,
                                                   PersistentSubscriptionListener listener) {
        this.connection = connection;
        this.group = group;
        this.options = options;
        this.listener = listener;
    }

    protected abstract Persistent.ReadReq.Options.Builder createOptions();

    public CompletableFuture<PersistentSubscription> execute() {
        return this.connection.runWithArgs(args -> {
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client =
                    GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), this.connection.getSettings(), this.options);

            final CompletableFuture<PersistentSubscription> result = new CompletableFuture<>();

            int bufferSize = this.options.getBufferSize();

            Persistent.ReadReq req = Persistent.ReadReq.newBuilder()
                    .setOptions(createOptions()
                            .setBufferSize(bufferSize)
                            .setGroupName(group))
                    .build();

            if (req.getOptions().hasAll() && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeatureException());
            } else {

                ClientResponseObserver<Persistent.ReadReq, Persistent.ReadResp> observer = new ClientResponseObserver<Persistent.ReadReq, Persistent.ReadResp>() {
                    private boolean _confirmed;
                    private PersistentSubscription _subscription;
                    private ClientCallStreamObserver<Persistent.ReadReq> _requestStream;

                    @Override
                    public void beforeStart(ClientCallStreamObserver<Persistent.ReadReq> requestStream) {
                        this._requestStream = requestStream;
                    }

                    @Override
                    public void onNext(Persistent.ReadResp readResp) {
                        if (!_confirmed && readResp.hasSubscriptionConfirmation()) {
                            this._confirmed = true;
                            this._subscription = new PersistentSubscription(this._requestStream,
                                    readResp.getSubscriptionConfirmation().getSubscriptionId());
                            result.complete(this._subscription);
                            listener.onConfirmation(this._subscription);
                            return;
                        }

                        if (!_confirmed && readResp.hasEvent()) {
                            onError(new IllegalStateException("Unconfirmed persistent subscription received event"));
                            return;
                        }

                        if (_confirmed && !readResp.hasEvent()) {
                            onError(new IllegalStateException(
                                    String.format("Confirmed persistent subscription %s received non-{event,checkpoint} variant",
                                            _subscription.getSubscriptionId())));
                            return;
                        }

                        int retryCount = readResp.getEvent().hasNoRetryCount() ? 0 : readResp.getEvent().getRetryCount();

                        listener.onEvent(this._subscription, retryCount, ResolvedEvent.fromWire(readResp.getEvent()));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (!_confirmed) {
                            result.completeExceptionally(throwable);
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

                StreamObserver<Persistent.ReadReq> wireStream = client.read(observer);
                wireStream.onNext(req);
            }

            return result;
        });
    }
}

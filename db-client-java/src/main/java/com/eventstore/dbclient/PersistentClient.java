package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PersistentClient {
    private static final Persistent.ReadReq.Options.Builder defaultReadOptions;

    private final ManagedChannel _channel;
    private final PersistentSubscriptionsGrpc.PersistentSubscriptionsStub _stub;
    private final Timeouts _timeouts;

    static {
        defaultReadOptions = Persistent.ReadReq.Options.newBuilder()
                .setUuidOption(Persistent.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
    }

    public PersistentClient(
            @NotNull ManagedChannel channel,
            UserCredentials credentials,
            boolean requiresLeader,
            @NotNull Timeouts timeouts) {
        _channel = channel;
        _timeouts = timeouts;

        Metadata headers = new Metadata();

        if (credentials != null) {
            headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), credentials.basicAuthHeader());
        }

        if (requiresLeader) {
            headers.put(Metadata.Key.of("requires-leader", Metadata.ASCII_STRING_MARSHALLER), String.valueOf(requiresLeader));
        }

        _stub = MetadataUtils.attachHeaders(PersistentSubscriptionsGrpc.newStub(_channel), headers);
    }

    public void shutdown() throws InterruptedException {
        _channel.shutdown().awaitTermination(_timeouts.shutdownTimeout, _timeouts.shutdownTimeoutUnit);
    }

    public CompletableFuture create(PersistentSubscriptionSettings settings, String stream, String group) {
        CompletableFuture result = new CompletableFuture();
        Persistent.CreateReq.Options.Builder builder = Persistent.CreateReq.Options.newBuilder();
        Persistent.CreateReq.Settings.Builder settingsBuilder = Persistent.CreateReq.Settings.newBuilder();
        Shared.StreamIdentifier.Builder streamIdentifierBuilder = Shared.StreamIdentifier.newBuilder();

        settingsBuilder.setRevision(settings.getRevision())
                .setResolveLinks(settings.isResolveLinks())
                .setReadBatchSize(settings.getReadBatchSize())
                .setMinCheckpointCount(settings.getMinCheckpointCount())
                .setMaxCheckpointCount(settings.getMaxCheckpointCount())
                .setMessageTimeoutMs(settings.getMessageTimeoutMs())
                .setMaxSubscriberCount(settings.getMaxSubscriberCount())
                .setMaxRetryCount(settings.getMaxRetryCount())
                .setLiveBufferSize(settings.getLiveBufferSize())
                .setHistoryBufferSize(settings.getHistoryBufferSize())
                .setExtraStatistics(settings.isExtraStatistics())
                .setCheckpointAfterMs(settings.getCheckpointAfterMs());

        switch (settings.getStrategy()) {
            case DispatchToSingle:
                settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.DispatchToSingle);
                break;
            case RoundRobin:
                settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.RoundRobin);
                break;
            case Pinned:
                settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.Pinned);
                break;
        }

        streamIdentifierBuilder.setStreamName(ByteString.copyFromUtf8(stream));

        builder.setSettings(settingsBuilder)
                .setGroupName(group)
                .setStreamIdentifier(streamIdentifierBuilder)
                .build();

        Persistent.CreateReq req = Persistent.CreateReq.newBuilder()
                .setOptions(builder)
                .build();

        _stub.create(req, convertSingleResponse(result, resp -> {
            return resp;
        }));

        return result;
    }

    public CompletableFuture update(PersistentSubscriptionSettings settings, String stream, String group) {
        CompletableFuture result = new CompletableFuture();
        Persistent.UpdateReq.Options.Builder builder = Persistent.UpdateReq.Options.newBuilder();
        Persistent.UpdateReq.Settings.Builder settingsBuilder = Persistent.UpdateReq.Settings.newBuilder();
        Shared.StreamIdentifier.Builder streamIdentifierBuilder = Shared.StreamIdentifier.newBuilder();

        settingsBuilder.setRevision(settings.getRevision())
                .setResolveLinks(settings.isResolveLinks())
                .setReadBatchSize(settings.getReadBatchSize())
                .setMinCheckpointCount(settings.getMinCheckpointCount())
                .setMaxCheckpointCount(settings.getMaxCheckpointCount())
                .setMessageTimeoutMs(settings.getMessageTimeoutMs())
                .setMaxSubscriberCount(settings.getMaxSubscriberCount())
                .setMaxRetryCount(settings.getMaxRetryCount())
                .setLiveBufferSize(settings.getLiveBufferSize())
                .setHistoryBufferSize(settings.getHistoryBufferSize())
                .setExtraStatistics(settings.isExtraStatistics())
                .setCheckpointAfterMs(settings.getCheckpointAfterMs());

        switch (settings.getStrategy()) {
            case DispatchToSingle:
                settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.DispatchToSingle);
                break;
            case RoundRobin:
                settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.RoundRobin);
                break;
            case Pinned:
                settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.Pinned);
                break;
        }

        streamIdentifierBuilder.setStreamName(ByteString.copyFromUtf8(stream));

        builder.setSettings(settingsBuilder)
                .setGroupName(group)
                .setStreamIdentifier(streamIdentifierBuilder)
                .build();

        Persistent.UpdateReq req = Persistent.UpdateReq.newBuilder()
                .setOptions(builder)
                .build();

        _stub.update(req, convertSingleResponse(result, resp -> {
            return resp;
        }));

        return result;
    }

    public CompletableFuture delete(String stream, String group) {
        CompletableFuture result = new CompletableFuture();

        Shared.StreamIdentifier streamIdentifier =
                Shared.StreamIdentifier.newBuilder()
                .setStreamName(ByteString.copyFromUtf8(stream))
                .build();

        Persistent.DeleteReq.Options options = Persistent.DeleteReq.Options.newBuilder()
                .setStreamIdentifier(streamIdentifier)
                .setGroupName(group)
                .build();

        Persistent.DeleteReq req = Persistent.DeleteReq.newBuilder()
                .setOptions(options)
                .build();

        _stub.delete(req, convertSingleResponse(result, resp -> resp));

        return result;
    }

    public CompletableFuture<PersistentSubscription> connect(String stream, String group, int bufferSize, PersistentSubscriptionListener listener) {
        final CompletableFuture<PersistentSubscription> result = new CompletableFuture<>();

        Shared.StreamIdentifier streamIdentifier =
                Shared.StreamIdentifier.newBuilder()
                .setStreamName(ByteString.copyFromUtf8(stream))
                .build();

        Persistent.ReadReq.Options options = defaultReadOptions.clone()
                .setBufferSize(bufferSize)
                .setStreamIdentifier(streamIdentifier)
                .setGroupName(group)
                .build();

        Persistent.ReadReq req = Persistent.ReadReq.newBuilder()
                .setOptions(options)
                .build();

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
                    this._subscription = new PersistentSubscription(this._requestStream, readResp.getSubscriptionConfirmation().getSubscriptionId(), stream, group, bufferSize, defaultReadOptions);
                    result.complete(this._subscription);
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

                listener.onEvent(this._subscription, ResolvedEvent.fromWire(readResp.getEvent()));
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusRuntimeException) {
                    Status s = ((StatusRuntimeException) t).getStatus();
                    if (s.getCode() == Status.Code.CANCELLED) {
                        listener.onCancelled(this._subscription);
                        return;
                    }
                }

                listener.onError(this._subscription, t);
            }

            @Override
            public void onCompleted() {
                // Subscriptions should only complete on error.
            }
        };

        StreamObserver<Persistent.ReadReq> wireStream = _stub.read(observer);
        wireStream.onNext(req);

        return result;
    }

    private <ReqT, RespT, TargetT> ClientResponseObserver<ReqT, RespT> convertSingleResponse(
            CompletableFuture<TargetT> dest, Function<RespT, TargetT> converter) {
        return new ClientResponseObserver<ReqT, RespT>() {
            @Override
            public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
            }

            @Override
            public void onNext(RespT value) {
                try {
                    TargetT converted = converter.apply(value);
                    dest.complete(converted);
                } catch (Throwable e) {
                    dest.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                dest.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}


package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsGrpc;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class StreamsClient {
    private static final StreamsOuterClass.ReadReq.Options.Builder defaultReadOptions;
    private static final StreamsOuterClass.ReadReq.Options.Builder defaultSubscribeOptions;

    private final ManagedChannel _channel;
    private final StreamsGrpc.StreamsStub _stub;
    private final Timeouts _timeouts;

    static {
        defaultReadOptions = StreamsOuterClass.ReadReq.Options.newBuilder()
                .setUuidOption(StreamsOuterClass.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
        defaultSubscribeOptions = defaultReadOptions.clone()
                .setReadDirection(StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards)
                .setSubscription(StreamsOuterClass.ReadReq.Options.SubscriptionOptions.getDefaultInstance());
    }

    // region Construction and Shutdown

    public StreamsClient(String host, int port, UserCredentials defaultCredentials, Timeouts timeouts, SslContext sslContext) {
        this(NettyChannelBuilder.forAddress(host, port)
                .userAgent("Event Store Client (Java) v1.0.0-SNAPSHOT")
                .sslContext(sslContext)
                .build(), defaultCredentials, false, timeouts);
    }

    public StreamsClient(
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

        _stub = MetadataUtils.attachHeaders(StreamsGrpc.newStub(_channel), headers);
    }

    public void shutdown() throws InterruptedException {
        _channel.shutdown().awaitTermination(_timeouts.shutdownTimeout, _timeouts.shutdownTimeoutUnit);
    }

    // endregion

    // region Append To Stream

    public CompletableFuture<WriteResult> appendToStream(
            @NotNull String streamName,
            @NotNull StreamRevision expectedRevision,
            @NotNull List<ProposedEvent> proposedEvents) {
        StreamsOuterClass.AppendReq.Options.Builder options = StreamsOuterClass.AppendReq.Options.newBuilder()
                .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                        .setStreamName(ByteString.copyFromUtf8(streamName))
                        .build())
                .setRevision(expectedRevision.getValueUnsigned());

        return appendInternal(options, proposedEvents);
    }

    public CompletableFuture<WriteResult> appendToStream(
            @NotNull String streamName,
            @NotNull SpecialStreamRevision expectedRevision,
            @NotNull List<ProposedEvent> proposedEvents) {
        StreamsOuterClass.AppendReq.Options.Builder options = StreamsOuterClass.AppendReq.Options.newBuilder()
                .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                        .setStreamName(ByteString.copyFromUtf8(streamName))
                        .build());

        switch (expectedRevision) {
            case NO_STREAM:
                options.setNoStream(Shared.Empty.getDefaultInstance());
                break;
            case STREAM_EXISTS:
                options.setStreamExists(Shared.Empty.getDefaultInstance());
                break;
            case ANY:
                options.setAny(Shared.Empty.getDefaultInstance());
                break;
        }

        return appendInternal(options, proposedEvents);
    }

    // endregion

    // region Read From Stream

    public CompletableFuture<ReadResult> readStream(
            @NotNull Direction direction,
            @NotNull String streamName,
            @NotNull StreamRevision from,
            int count,
            boolean resolveLinks
    ) {
        StreamsOuterClass.ReadReq.Options.Builder opts = defaultReadOptions.clone()
                .setStream(toStreamOptions(streamName, from))
                .setResolveLinks(resolveLinks)
                .setCount(count)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(direction == Direction.Forward ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);
        StreamsOuterClass.ReadReq request = StreamsOuterClass.ReadReq.newBuilder()
                .setOptions(opts)
                .build();
        return readInternal(request);
    }

    public CompletableFuture<ReadResult> readAll(
            Direction direction,
            Position position,
            int count,
            boolean resolveLinks
    ) {
        StreamsOuterClass.ReadReq.Options.Builder opts = defaultReadOptions.clone()
                .setAll(StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder()
                        .setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                                .setCommitPosition(position.getCommitUnsigned())
                                .setPreparePosition(position.getPrepareUnsigned())))
                .setResolveLinks(resolveLinks)
                .setCount(count)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(direction == Direction.Forward ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);
        StreamsOuterClass.ReadReq request = StreamsOuterClass.ReadReq.newBuilder()
                .setOptions(opts)
                .build();
        return readInternal(request);
    }

    // endregion

    // region Soft Deletion

    public CompletableFuture<DeleteResult> softDelete(
            @NotNull String streamName,
            @NotNull StreamRevision expectedRevision
    ) {
        StreamsOuterClass.DeleteReq req = StreamsOuterClass.DeleteReq.newBuilder()
                .setOptions(StreamsOuterClass.DeleteReq.Options.newBuilder()
                        .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                                .setStreamName(ByteString.copyFromUtf8(streamName))
                                .build())
                        .setRevision(expectedRevision.getValueUnsigned()))
                .build();

        CompletableFuture<DeleteResult> result = new CompletableFuture<>();
        _stub.delete(req, convertSingleResponse(result, resp -> {
            final long commitUnsigned = resp.getPosition().getCommitPosition();
            final long prepareUnsigned = resp.getPosition().getPreparePosition();

            return new DeleteResult(new Position(commitUnsigned, prepareUnsigned));
        }));
        return result;
    }

    // endregion

    // region Subscriptions

    public CompletableFuture<Subscription> subscribeToStream(String streamName,
                                                             StreamRevision lastCheckpoint,
                                                             boolean resolveLinks,
                                                             SubscriptionListener listener) {
        StreamsOuterClass.ReadReq.Options.Builder opts = defaultSubscribeOptions.clone()
                .setResolveLinks(resolveLinks)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setStream(toStreamOptions(streamName, lastCheckpoint));

        return subscribeInternal(opts, listener, null);
    }

    public CompletableFuture<Subscription> subscribeToAll(Position lastCheckpoint,
                                                          boolean resolveLinks,
                                                          SubscriptionListener listener) {
        return subscribeToAll(lastCheckpoint, resolveLinks, listener, null);
    }

    public CompletableFuture<Subscription> subscribeToAll(Position lastCheckpoint,
                                                          boolean resolveLinks,
                                                          SubscriptionListener listener,
                                                          SubscriptionFilter filter) {
        StreamsOuterClass.ReadReq.Options.Builder opts = defaultSubscribeOptions.clone()
                .setResolveLinks(resolveLinks)
                .setAll(StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder()
                        .setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                                .setCommitPosition(lastCheckpoint.getCommitUnsigned())
                                .setPreparePosition(lastCheckpoint.getPrepareUnsigned()))
                        .build());
        Checkpointer checkpointer = null;
        if (filter != null) {
            filter.addToWireReadReq(opts);
            checkpointer = filter.getCheckpointer();
        } else {
            opts.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        return subscribeInternal(opts, listener, checkpointer);
    }

    // endregion

    // region Internal Implementation Helpers

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
                if (t instanceof StatusRuntimeException) {
                    StatusRuntimeException e = (StatusRuntimeException) t;
                    String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                    String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                    if (leaderHost != null && leaderPort != null) {
                        NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                        dest.completeExceptionally(reason);
                        return;
                    }
                }

                dest.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }

    private CompletableFuture<ReadResult> readInternal(StreamsOuterClass.ReadReq request) {
        CompletableFuture<ReadResult> future = new CompletableFuture<>();
        ArrayList<ResolvedEvent> resolvedEvents = new ArrayList<>();

        _stub.read(request, new StreamObserver<StreamsOuterClass.ReadResp>() {
            private boolean completed = false;

            @Override
            public void onNext(StreamsOuterClass.ReadResp value) {
                if (value.hasStreamNotFound()) {
                    future.completeExceptionally(new StreamNotFoundException());
                    this.completed = true;
                    return;
                }

                if (value.hasEvent()) {
                    resolvedEvents.add(ResolvedEvent.fromWire(value.getEvent()));
                }
            }

            @Override
            public void onCompleted() {
                if (this.completed) {
                    return;
                }

                future.complete(new ReadResult(resolvedEvents));
            }

            @Override
            public void onError(Throwable t) {
                if (this.completed) {
                    return;
                }

                if (t instanceof StatusRuntimeException) {
                    StatusRuntimeException e = (StatusRuntimeException) t;
                    String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                    String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                    if (leaderHost != null && leaderPort != null) {
                        NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                        future.completeExceptionally(reason);
                        return;
                    }
                }

                future.completeExceptionally(t);
            }
        });
        return future;
    }

    private CompletableFuture<WriteResult> appendInternal(
            @NotNull StreamsOuterClass.AppendReq.Options.Builder options,
            @NotNull List<ProposedEvent> proposedEvents) {
        CompletableFuture<WriteResult> result = new CompletableFuture<>();
        StreamObserver<StreamsOuterClass.AppendReq> requestStream = _stub.append(convertSingleResponse(result, resp -> {
            if (resp.hasSuccess()) {
                StreamsOuterClass.AppendResp.Success success = resp.getSuccess();

                StreamRevision nextExpectedRevision;
                if (success.getCurrentRevisionOptionCase() == StreamsOuterClass.AppendResp.Success.CurrentRevisionOptionCase.NO_STREAM) {
                    nextExpectedRevision = new StreamRevision(1); // NO_STREAM
                } else {
                    nextExpectedRevision = new StreamRevision(success.getCurrentRevision());
                }

                Position logPosition = null;
                if (success.getPositionOptionCase() == StreamsOuterClass.AppendResp.Success.PositionOptionCase.POSITION) {
                    StreamsOuterClass.AppendResp.Position p = success.getPosition();
                    logPosition = new Position(p.getCommitPosition(), p.getPreparePosition());
                }

                return new WriteResult(nextExpectedRevision, logPosition);
            }
            if (resp.hasWrongExpectedVersion()) {
                StreamsOuterClass.AppendResp.WrongExpectedVersion wev = resp.getWrongExpectedVersion();

                StreamRevision expectedRevision;
                if (wev.getExpectedRevisionOptionCase() == StreamsOuterClass.AppendResp.WrongExpectedVersion.ExpectedRevisionOptionCase.ANY) {
                    expectedRevision = new StreamRevision(2); // StreamState.Constants.Any;
                } else if (wev.getExpectedRevisionOptionCase() == StreamsOuterClass.AppendResp.WrongExpectedVersion.ExpectedRevisionOptionCase.STREAM_EXISTS) {
                    expectedRevision = new StreamRevision(4); // StreamState.Constants.StreamExists;
                } else {
                    expectedRevision = new StreamRevision(wev.getExpectedRevision());
                }

                StreamRevision currentRevision;
                if (wev.getCurrentRevisionOptionCase() == StreamsOuterClass.AppendResp.WrongExpectedVersion.CurrentRevisionOptionCase.NO_STREAM) {
                    // TODO(jen20): This feels very wrong?
                    currentRevision = new StreamRevision(2); //StreamState.Constants.NoStream
                } else {
                    currentRevision = new StreamRevision(wev.getCurrentRevision());
                }

                String streamName = options.getStreamIdentifier().getStreamName().toStringUtf8();

                throw new WrongExpectedVersionException(streamName, expectedRevision, currentRevision);
            }

            throw new IllegalStateException("AppendResponse has neither Success or WrongExpectedVersion variants");
        }));

        try {
            requestStream.onNext(StreamsOuterClass.AppendReq.newBuilder().setOptions(options).build());

            for (ProposedEvent e : proposedEvents) {
                requestStream.onNext(StreamsOuterClass.AppendReq.newBuilder()
                        .setProposedMessage(StreamsOuterClass.AppendReq.ProposedMessage.newBuilder()
                                .setId(Shared.UUID.newBuilder()
                                        .setStructured(Shared.UUID.Structured.newBuilder()
                                                .setMostSignificantBits(e.getEventId().getMostSignificantBits())
                                                .setLeastSignificantBits(e.getEventId().getLeastSignificantBits())))
                                .setData(ByteString.copyFrom(e.getEventData()))
                                .setCustomMetadata(ByteString.copyFrom(e.getUserMetadata()))
                                .putMetadata(SystemMetadataKeys.CONTENT_TYPE, e.getContentType())
                                .putMetadata(SystemMetadataKeys.TYPE, e.getEventType()))
                        .build());
            }
            requestStream.onCompleted();
        } catch (StatusRuntimeException e) {
            String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
            String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

            if (leaderHost != null && leaderPort != null) {
                NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                result.completeExceptionally(reason);
            } else {
                result.completeExceptionally(e);
            }
        } catch (RuntimeException e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    private CompletableFuture<Subscription> subscribeInternal(StreamsOuterClass.ReadReq.Options.Builder opts, SubscriptionListener listener, Checkpointer checkpointer) {
        StreamsOuterClass.ReadReq readReq = StreamsOuterClass.ReadReq.newBuilder()
                .setOptions(opts)
                .build();

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
                if (throwable instanceof StatusRuntimeException) {
                    StatusRuntimeException e = (StatusRuntimeException) throwable;
                    if (e.getStatus().getCode() == Status.Code.CANCELLED) {
                        listener.onCancelled(this._subscription);
                        return;
                    }

                    String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                    String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                    if (leaderHost != null && leaderPort != null) {
                        NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                        listener.onError(this._subscription, reason);
                    }
                }

                listener.onError(this._subscription, throwable);
            }

            @Override
            public void onCompleted() {
                // Subscriptions should only complete on error.
            }
        };

        _stub.read(readReq, observer);

        return future;
    }

    // endregion

    // region Protocol Buffers Conversion

    private static StreamsOuterClass.ReadReq.Options.StreamOptions toStreamOptions(String streamName, StreamRevision revision) {
        StreamsOuterClass.ReadReq.Options.StreamOptions.Builder builder = StreamsOuterClass.ReadReq.Options.StreamOptions.newBuilder()
                .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                        .setStreamName(ByteString.copyFromUtf8(streamName))
                        .build());

        if (revision == StreamRevision.END) {
            return builder.setEnd(Shared.Empty.getDefaultInstance())
                    .build();
        }

        if (revision == StreamRevision.START) {
            return builder.setStart(Shared.Empty.getDefaultInstance())
                    .build();
        }

        return builder.setRevision(revision.getValueUnsigned())
                .build();
    }

    // endregion
}

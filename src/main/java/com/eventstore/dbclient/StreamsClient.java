package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsGrpc;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

public class StreamsClient {
    private static final StreamsOuterClass.ReadReq.Options.Builder defaultReadOptions;

    private final ManagedChannel _channel;
    private final StreamsGrpc.StreamsStub _stub;

    static {
        defaultReadOptions = StreamsOuterClass.ReadReq.Options.newBuilder()
                .setUuidOption(StreamsOuterClass.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
    }

    // region Construction and Shutdown

    public StreamsClient(String host, int port, UserCredentials defaultCredentials, SslContext sslContext) {
        this(NettyChannelBuilder.forAddress(host, port)
                .userAgent("Event Store Client (Java) v1.0.0-SNAPSHOT")
                .sslContext(sslContext)
                .build(), defaultCredentials);
    }

    public StreamsClient(ManagedChannel channel, UserCredentials credentials) {
        _channel = channel;

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), credentials.basicAuthHeader());

        _stub = MetadataUtils.attachHeaders(StreamsGrpc.newStub(_channel), headers);
    }

    public void shutdown() throws InterruptedException {
        // TODO(jen20): Make a configurable timeout here
        _channel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
    }

    // endregion

    // region Append To Stream

    public CompletableFuture<WriteResult> appendToStream(
            @NotNull String streamName,
            @NotNull StreamRevision expectedRevision,
            @NotNull List<ProposedEvent> proposedEvents) {
        StreamsOuterClass.AppendReq.Options.Builder options = StreamsOuterClass.AppendReq.Options.newBuilder()
                .setStreamName(streamName)
                .setRevision(expectedRevision.getValueUnsigned());

        return appendInternal(options, proposedEvents);
    }

    public CompletableFuture<WriteResult> appendToStream(
            @NotNull String streamName,
            @NotNull SpecialStreamRevision expectedRevision,
            @NotNull List<ProposedEvent> proposedEvents) {
        StreamsOuterClass.AppendReq.Options.Builder options = StreamsOuterClass.AppendReq.Options.newBuilder()
                .setStreamName(streamName);

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

    public CompletableFuture<ReadStreamResult> readStream(
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

        CompletableFuture<ReadStreamResult> result = new CompletableFuture<>();
        ArrayList<ResolvedEvent> resolvedEvents = new ArrayList<>();

        _stub.read(request, new StreamObserver<StreamsOuterClass.ReadResp>() {
            private boolean completed = false;

            @Override
            public void onNext(StreamsOuterClass.ReadResp value) {
                if (value.hasStreamNotFound()) {
                    result.completeExceptionally(new StreamNotFoundException());
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

                result.complete(new ReadStreamResult(resolvedEvents));
            }

            @Override
            public void onError(Throwable t) {
                if (this.completed) {
                    return;
                }

                result.completeExceptionally(t);
            }
        });

        return result;
    }

    // endregion

    // region Soft Deletion

    public CompletableFuture<DeleteResult> softDelete(
            @NotNull String streamName,
            @NotNull StreamRevision expectedRevision
    ) {
        StreamsOuterClass.DeleteReq req = StreamsOuterClass.DeleteReq.newBuilder()
                .setOptions(StreamsOuterClass.DeleteReq.Options.newBuilder()
                        .setStreamName(streamName)
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
                dest.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        };
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

                throw new WrongExpectedVersionException(options.getStreamName(), expectedRevision, currentRevision);
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
        } catch (RuntimeException e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    // endregion

    // region Protocol Buffers Conversion

    private static StreamsOuterClass.ReadReq.Options.StreamOptions toStreamOptions(String streamName, StreamRevision revision) {
        StreamsOuterClass.ReadReq.Options.StreamOptions.Builder builder = StreamsOuterClass.ReadReq.Options.StreamOptions.newBuilder()
                .setStreamName(streamName);

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

    private static void addFilter(StreamsOuterClass.ReadReq.Options.Builder builder, EventFilter filter) {
        if (filter == null) {
            builder.setNoFilter(Shared.Empty.getDefaultInstance());
            return;
        }

        RegularFilterExpression regex = filter.getRegularFilterExpression();
        PrefixFilterExpression[] prefixes = filter.getPrefixFilterExpressions();
        Optional<Integer> maxSearchWindow = filter.getMaxSearchWindow();

        if (regex != null && prefixes != null && prefixes.length != 0) {
            throw new IllegalArgumentException("Regex and Prefix expressions are mutually exclusive");
        }

        StreamsOuterClass.ReadReq.Options.FilterOptions.Expression expression = null;
        if (regex != null) {
            expression = StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.newBuilder()
                    .setRegex(regex.toString())
                    .build();
        }

        if (prefixes != null && prefixes.length > 0) {
            StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.Builder expressionB = StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.newBuilder();
            Stream.of(prefixes)
                    .map(Object::toString)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(expressionB::addPrefix);
            expression = expressionB.build();
        }

        if (expression == null) {
            builder.setNoFilter(Shared.Empty.getDefaultInstance());
            return;
        }

        StreamsOuterClass.ReadReq.Options.FilterOptions.Builder optsB = StreamsOuterClass.ReadReq.Options.FilterOptions.newBuilder();
        if (filter instanceof StreamFilter) {
            optsB.setStreamName(expression);
        }
        if (filter instanceof EventTypeFilter) {
            optsB.setEventType(expression);
        }

        if (maxSearchWindow != null && maxSearchWindow.isPresent()) {
            optsB.setMax(maxSearchWindow.get());
        } else {
            optsB.setCount(Shared.Empty.getDefaultInstance());
        }

        builder.setFilter(optsB.build());
    }


    // endregion
}

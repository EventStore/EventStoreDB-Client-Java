package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsGrpc;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AppendToStream {
    private GrpcClient client;
    private String streamName;
    private ExpectedRevision expectedRevision;
    private List<EventData> events;
    private Timeouts timeouts;
    private ConnectionMetadata metadata;

    public AppendToStream(GrpcClient client, String streamName, UserCredentials credentials) {
        this.client = client;
        this.streamName = streamName;
        this.expectedRevision = ExpectedRevision.ANY;
        this.events = new ArrayList<>();
        this.timeouts = Timeouts.DEFAULT;
        this.metadata = new ConnectionMetadata();

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    public AppendToStream authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public AppendToStream expectedRevision(ExpectedRevision revision) {
        this.expectedRevision = revision;
        return this;
    }

    public AppendToStream timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public AppendToStream requiresLeader() {
        return requiresLeader(true);
    }

    public AppendToStream notRequireLeader() {
        return requiresLeader(false);
    }

    public AppendToStream requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public AppendToStream addEvent(EventData event) {
        this.events.add(event);

        return this;
    }

    public AppendToStream addEvents(EventData... events) {
        this.addEvents(Arrays.stream(events).iterator());
        return this;
    }

    public AppendToStream addEvents(Iterator<EventData> events) {
        while (events.hasNext()) {
            this.events.add(events.next());
        }

        return this;
    }

    public CompletableFuture<WriteResult> execute() {
        return this.client.run(channel -> {
            Metadata headers = this.metadata.build();
            CompletableFuture<WriteResult> result = new CompletableFuture<>();
            StreamsOuterClass.AppendReq.Options.Builder options = this.expectedRevision.applyOnWire(StreamsOuterClass.AppendReq.Options.newBuilder()
                    .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                            .setStreamName(ByteString.copyFromUtf8(streamName))
                            .build()));
            StreamsGrpc.StreamsStub client = MetadataUtils.attachHeaders(StreamsGrpc.newStub(channel), headers);

            StreamObserver<StreamsOuterClass.AppendReq> requestStream = client.append(GrpcUtils.convertSingleResponse(result, resp -> {
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

                for (EventData e : this.events) {
                    StreamsOuterClass.AppendReq.ProposedMessage.Builder msgBuilder = StreamsOuterClass.AppendReq.ProposedMessage.newBuilder()
                            .setId(Shared.UUID.newBuilder()
                                    .setStructured(Shared.UUID.Structured.newBuilder()
                                            .setMostSignificantBits(e.getEventId().getMostSignificantBits())
                                            .setLeastSignificantBits(e.getEventId().getLeastSignificantBits())))
                            .setData(ByteString.copyFrom(e.getEventData()))
                            .putMetadata(SystemMetadataKeys.CONTENT_TYPE, e.getContentType())
                            .putMetadata(SystemMetadataKeys.TYPE, e.getEventType());

                    if (e.getUserMetadata() != null) {
                        msgBuilder.setCustomMetadata(ByteString.copyFrom(e.getUserMetadata()));
                    }

                    requestStream.onNext(StreamsOuterClass.AppendReq.newBuilder()
                            .setProposedMessage(msgBuilder)
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
        });
    }
}

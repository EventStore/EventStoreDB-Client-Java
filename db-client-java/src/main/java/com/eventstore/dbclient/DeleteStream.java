package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsGrpc;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public class DeleteStream {
    private EventStoreNodeConnection connection;
    private String streamName;
    private ExpectedRevision expectedRevision;
    private ConnectionMetadata metadata;
    private Timeouts timeouts;
    private boolean softDelete;

    public DeleteStream(EventStoreNodeConnection connection, String streamName, UserCredentials credentials) {
        this.connection = connection;
        this.streamName = streamName;
        this.metadata = new ConnectionMetadata();
        this.timeouts = Timeouts.DEFAULT;
        this.expectedRevision = ExpectedRevision.ANY;
        this.softDelete = true;

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    public DeleteStream softDelete() {
        this.softDelete = true;
        return this;
    }

    public DeleteStream hardDelete() {
        this.softDelete = false;
        return this;
    }

    public DeleteStream requiresLeader() {
        return requiresLeader(true);
    }

    public DeleteStream notRequireLeader() {
        return requiresLeader(false);
    }

    public DeleteStream requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public DeleteStream authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public DeleteStream expectedRevision(ExpectedRevision revision) {
        this.expectedRevision = revision;
        return this;
    }

    public DeleteStream timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public CompletableFuture<DeleteResult> execute() {
        return this.connection.run(channel -> {
            Metadata headers = this.metadata.build();
            StreamsGrpc.StreamsStub client = MetadataUtils.attachHeaders(StreamsGrpc.newStub(channel), headers);

            if (this.softDelete) {
                StreamsOuterClass.DeleteReq req = StreamsOuterClass.DeleteReq.newBuilder()
                        .setOptions(this.expectedRevision.applyOnWire(StreamsOuterClass.DeleteReq.Options.newBuilder()
                                .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                                        .setStreamName(ByteString.copyFromUtf8(streamName))
                                        .build())))
                        .build();

                CompletableFuture<DeleteResult> result = new CompletableFuture<>();
                client.delete(req, GrpcUtils.convertSingleResponse(result, resp -> {
                    final long commitUnsigned = resp.getPosition().getCommitPosition();
                    final long prepareUnsigned = resp.getPosition().getPreparePosition();

                    return new DeleteResult(new Position(commitUnsigned, prepareUnsigned));
                }));
                return result;
            }

            StreamsOuterClass.TombstoneReq req = StreamsOuterClass.TombstoneReq.newBuilder()
                    .setOptions(this.expectedRevision.applyOnWire(StreamsOuterClass.TombstoneReq.Options.newBuilder()
                            .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                                    .setStreamName(ByteString.copyFromUtf8(streamName))
                                    .build())))
                    .build();

            CompletableFuture<DeleteResult> result = new CompletableFuture<>();
            client.tombstone(req, GrpcUtils.convertSingleResponse(result, resp -> {
                final long commitUnsigned = resp.getPosition().getCommitPosition();
                final long prepareUnsigned = resp.getPosition().getPreparePosition();

                return new DeleteResult(new Position(commitUnsigned, prepareUnsigned));
            }));
            return result;
        });
    }
}

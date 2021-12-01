package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public class DeleteProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final DeleteProjectionOptions options;

    public DeleteProjection(final GrpcClient client, final String projectionName, final DeleteProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.DeleteReq.Options.Builder optionsBuilder =
                    Projectionmanagement.DeleteReq.Options.newBuilder()
                            .setName(this.projectionName)
                            .setDeleteCheckpointStream(options.getDeleteCheckpointStream())
                            .setDeleteEmittedStreams(options.getDeleteEmittedStreams())
                            .setDeleteStateStream(options.getDeleteStateStream());

            Projectionmanagement.DeleteReq request = Projectionmanagement.DeleteReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils
                    .attachHeaders(ProjectionsGrpc.newStub(channel), options.getMetadata());

            CompletableFuture<Projectionmanagement.DeleteResp> result = new CompletableFuture<>();

            client.delete(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

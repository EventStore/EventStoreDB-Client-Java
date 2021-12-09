package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public class ResetProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final Metadata metadata;

    public ResetProjection(final GrpcClient client, final String projectionName, final ResetProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.metadata = options.getMetadata();
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.ResetReq.Options.Builder optionsBuilder =
                    Projectionmanagement.ResetReq.Options.newBuilder()
                            .setName(this.projectionName);

            Projectionmanagement.ResetReq request = Projectionmanagement.ResetReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils
                    .attachHeaders(ProjectionsGrpc.newStub(channel), this.metadata);

            CompletableFuture<Projectionmanagement.ResetResp> result = new CompletableFuture<>();

            client.reset(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public class DisableProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final Metadata metadata;

    public DisableProjection(final GrpcClient client, final String projectionName, final DisableProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.metadata = options.getMetadata();
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.DisableReq.Options.Builder optionsBuilder =
                    Projectionmanagement.DisableReq.Options.newBuilder()
                            .setName(this.projectionName)
                            .setWriteCheckpoint(true);

            Projectionmanagement.DisableReq request = Projectionmanagement.DisableReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils
                    .attachHeaders(ProjectionsGrpc.newStub(channel), this.metadata);

            CompletableFuture<Projectionmanagement.DisableResp> result = new CompletableFuture<>();

            client.disable(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

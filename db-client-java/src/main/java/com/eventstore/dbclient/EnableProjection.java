package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public class EnableProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final Metadata metadata;

    public EnableProjection(final GrpcClient client, final String projectionName, final EnableProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.metadata = options.getMetadata();
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.EnableReq.Options.Builder optionsBuilder =
                    Projectionmanagement.EnableReq.Options.newBuilder()
                            .setName(this.projectionName);

            Projectionmanagement.EnableReq request = Projectionmanagement.EnableReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils
                    .attachHeaders(ProjectionsGrpc.newStub(channel), this.metadata);

            CompletableFuture<Projectionmanagement.EnableResp> result = new CompletableFuture<>();

            client.enable(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

class UpdateProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final String query;
    private final Boolean emitEnabled;
    private final Metadata metadata;

    public UpdateProjection(final GrpcClient client, final String projectionName, final String query,
                            final UpdateProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.query = query;
        this.emitEnabled = options.isEmitEnabled();
        this.metadata = options.getMetadata();
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.UpdateReq.Options.Builder optionsBuilder =
                Projectionmanagement.UpdateReq.Options.newBuilder()
                    .setName(this.projectionName)
                    .setQuery(this.query);

            if(this.emitEnabled == null){
                optionsBuilder.setNoEmitOptions(Shared.Empty.newBuilder());
            } else {
                optionsBuilder.setEmitEnabled(this.emitEnabled);
            }

            Projectionmanagement.UpdateReq request = Projectionmanagement.UpdateReq.newBuilder()
                .setOptions(optionsBuilder)
                .build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils
                    .attachHeaders(ProjectionsGrpc.newStub(channel), this.metadata);

            CompletableFuture<Projectionmanagement.UpdateResp> result = new CompletableFuture<>();

            client.update(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

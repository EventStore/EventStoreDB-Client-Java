package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;


class GetProjectionStatistics {
    private final GrpcClient client;
    private final String projectionName;
    private final Metadata metadata;

    public GetProjectionStatistics(final GrpcClient client, final String projectionName, final GetProjectionStatisticsOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.metadata = options.getMetadata();
    }

    public CompletableFuture<ProjectionDetails> execute() {
        return this.client.run(channel -> {
            Projectionmanagement.StatisticsReq.Options.Builder optionsBuilder =
                    Projectionmanagement.StatisticsReq.Options.newBuilder()
                            .setName(this.projectionName);

            Projectionmanagement.StatisticsReq request = Projectionmanagement.StatisticsReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils
                    .attachHeaders(ProjectionsGrpc.newStub(channel), this.metadata);

            CompletableFuture<ProjectionDetails> result = new CompletableFuture<>();

            client.statistics(request, GrpcUtils.convertSingleResponse(result, resp -> {
                final Projectionmanagement.StatisticsResp.Details details = resp.getDetails();
                return ProjectionDetails.fromWire(details);
            }));

            return result;
        });
    }
}

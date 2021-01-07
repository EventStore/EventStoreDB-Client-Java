package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public class CreateProjection {

    private final GrpcClient client;
    private final boolean continuous;
    private final String projectionName;
    private final String query;
    private final boolean trackEmittedStreams;
    private final Metadata metadata;

    private CreateProjection(final GrpcClient client, final Metadata metadata, final boolean continuous,
                            final String projectionName, final String query, final boolean trackEmittedStreams) {

        this.client = client;
        this.metadata = metadata;
        this.continuous = continuous;
        this.projectionName = projectionName;
        this.query = query;
        this.trackEmittedStreams = trackEmittedStreams;
    }

    static CreateProjection forContinuous(final GrpcClient client,
                                                 final String projectionName, final String query,
                                                 final CreateContinuousProjectionOptions options) {

        return new CreateProjection(client, options.getMetadata(), true, projectionName, query, options.isTrackingEmittedStreams());
    }

    static CreateProjection forOneTime(final GrpcClient client,
                                          final String projectionName, final String query,
                                          CreateOneTimeProjectionOptions options) {

        return new CreateProjection(client, options.getMetadata(), false, projectionName, query, false);
    }

    public CompletableFuture execute() {

        return this.client.run(channel -> {

            Projectionmanagement.CreateReq.Options.Builder optionsBuilder =
                    Projectionmanagement.CreateReq.Options.newBuilder()
                        .setQuery(query);

            setContinuousOrOneTime(optionsBuilder);

            Projectionmanagement.CreateReq request = Projectionmanagement.CreateReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            Metadata headers = this.metadata;

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils.attachHeaders(ProjectionsGrpc.newStub(channel), headers);

            CompletableFuture<Projectionmanagement.CreateResp> result = new CompletableFuture<>();

            client.create(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }

    private void setContinuousOrOneTime(final Projectionmanagement.CreateReq.Options.Builder optionsBuilder) {

        if(continuous) {

            Projectionmanagement.CreateReq.Options.Continuous.Builder continuousBuilder =
                    Projectionmanagement.CreateReq.Options.Continuous.newBuilder();

            continuousBuilder.setTrackEmittedStreams(trackEmittedStreams);
            continuousBuilder.setName(projectionName);

            optionsBuilder.setContinuous(continuousBuilder);

        } else {

            optionsBuilder.setOneTime(Shared.Empty.newBuilder());
        }
    }
}

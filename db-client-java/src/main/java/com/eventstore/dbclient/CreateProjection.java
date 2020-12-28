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
    private final ConnectionMetadata metadata;

    private CreateProjection(final GrpcClient client, final UserCredentials credentials, final boolean continuous,
                            final String projectionName, final String query, final boolean trackEmittedStreams) {

        this.client = client;
        this.continuous = continuous;
        this.projectionName = projectionName;
        this.query = query;
        this.trackEmittedStreams = trackEmittedStreams;

        this.metadata = new ConnectionMetadata();

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    static CreateProjection forContinuous(final GrpcClient client, final UserCredentials credentials,
                                                 final String projectionName, final String query,
                                                 final boolean trackEmittedStreams) {

        return new CreateProjection(client, credentials, true, projectionName, query, trackEmittedStreams);
    }

    public CreateProjection authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
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

            Metadata headers = this.metadata.build();

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

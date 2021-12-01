package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public class RestartProjectionSubsystem {
    private final GrpcClient client;
    private final Metadata metadata;

    public RestartProjectionSubsystem(final GrpcClient client, final RestartProjectionSubsystemOptions options) {
        this.client = client;
        this.metadata = options.getMetadata();
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            ProjectionsGrpc.ProjectionsStub client = MetadataUtils
                    .attachHeaders(ProjectionsGrpc.newStub(channel), this.metadata);

            CompletableFuture<Shared.Empty> result = new CompletableFuture<>();

            Shared.Empty request = Shared.Empty.newBuilder().build();

            client.restartSubsystem(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

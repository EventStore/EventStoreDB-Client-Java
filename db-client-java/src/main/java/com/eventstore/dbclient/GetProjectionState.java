package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GetProjectionState<TResult> {

    private final GrpcClient client;
    private final String projectionName;
    private final String partition;

    private final ConnectionMetadata metadata;

    private ThrowingBiFunction<JsonMapper, String, TResult, JsonProcessingException> deserializationStrategy;

    GetProjectionState(final GrpcClient client, final UserCredentials credentials,
                       final String projectionName, Class<TResult> resultType) {

        this(client, credentials, projectionName, null, resultType);
    }

    GetProjectionState(final GrpcClient client, final UserCredentials credentials,
                       final String projectionName, final String partition, Class<TResult> resultType) {

        this(client, credentials, projectionName, partition);
        deserializationStrategy = ((jsonMapper, json) -> jsonMapper.readValue(json, resultType));
    }

    GetProjectionState(final GrpcClient client, final UserCredentials credentials,
                       final String projectionName, Function<TypeFactory, JavaType> javaTypeFunction) {

        this(client, credentials, projectionName, null, javaTypeFunction);
    }

    GetProjectionState(final GrpcClient client, final UserCredentials credentials,
                       final String projectionName, final String partition,
                       Function<TypeFactory, JavaType> javaTypeFunction) {

        this(client, credentials, projectionName, partition);
        deserializationStrategy = ((jsonMapper, json)
                -> jsonMapper.readValue(json, javaTypeFunction.apply(jsonMapper.getTypeFactory())));
    }

    private GetProjectionState(final GrpcClient client, final UserCredentials credentials,
                               final String projectionName, final String partition) {

        this.client = client;
        this.projectionName = projectionName;
        this.partition = partition;

        this.metadata = new ConnectionMetadata();

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }


    public GetProjectionState authenticated(UserCredentials credentials) {
        if(credentials == null)
            return this;

        this.metadata.authenticated(credentials);
        return this;
    }

    public CompletableFuture<TResult> execute() {

        return this.client.run(channel -> {

            Projectionmanagement.StateReq.Options.Builder optionsBuilder =
                    Projectionmanagement.StateReq.Options.newBuilder()
                            .setName(projectionName);

            if(partition != null && !partition.isEmpty()) {
                optionsBuilder.setPartition(partition);
            }

            Projectionmanagement.StateReq request = Projectionmanagement.StateReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            Metadata headers = this.metadata.build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils.attachHeaders(ProjectionsGrpc.newStub(channel), headers);

            CompletableFuture<TResult> result = new CompletableFuture<>();

            ThrowingFunction<Projectionmanagement.StateResp, TResult, Exception> converter = source -> {

                String json = JsonFormat.printer().print(source.getState());
                return deserializationStrategy.apply(new JsonMapper(), json);
            };

            client.state(request, GrpcUtils.convertSingleResponse(result, converter));

            return result;
        });
    }
}

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

public class GetProjectionResult<TResult> {

    private final GrpcClient client;
    private final String projectionName;

    private final ConnectionMetadata metadata;

    private ThrowingBiFunction<JsonMapper, String, TResult, JsonProcessingException> deserializationStrategy;

    GetProjectionResult(final GrpcClient client, final UserCredentials credentials,
                               final String projectionName, Class<TResult> resultType) {

        this(client, credentials, projectionName);
        deserializationStrategy = ((jsonMapper, json) -> jsonMapper.readValue(json, resultType));
    }

    GetProjectionResult(final GrpcClient client, final UserCredentials credentials,
                               final String projectionName,
                               Function<TypeFactory, JavaType> javaTypeFunction) {

        this(client, credentials, projectionName);
        deserializationStrategy = ((jsonMapper, json)
                -> jsonMapper.readValue(json, javaTypeFunction.apply(jsonMapper.getTypeFactory())));
    }

    private GetProjectionResult(final GrpcClient client, final UserCredentials credentials,
                               final String projectionName) {

        this.client = client;
        this.projectionName = projectionName;

        this.metadata = new ConnectionMetadata();

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }


    public GetProjectionResult authenticated(UserCredentials credentials) {
        if(credentials == null)
            return this;

        this.metadata.authenticated(credentials);
        return this;
    }

    public CompletableFuture<TResult> execute() {

        return this.client.run(channel -> {

            Projectionmanagement.ResultReq.Options.Builder optionsBuilder =
                    Projectionmanagement.ResultReq.Options.newBuilder()
                            .setName(projectionName);


            Projectionmanagement.ResultReq request = Projectionmanagement.ResultReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            Metadata headers = this.metadata.build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils.attachHeaders(ProjectionsGrpc.newStub(channel), headers);

            CompletableFuture<TResult> result = new CompletableFuture<>();

            ThrowingFunction<Projectionmanagement.ResultResp, TResult, Exception> converter = source -> {

                String json = JsonFormat.printer().print(source.getResult());
                return deserializationStrategy.apply(new JsonMapper(), json);
            };

            client.result(request, GrpcUtils.convertSingleResponse(result, converter));

            return result;
        });
    }
}

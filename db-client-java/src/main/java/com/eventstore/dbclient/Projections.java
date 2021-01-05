package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Projections {

    private GrpcClient client;
    private UserCredentials credentials;

    public Projections(final GrpcClient client, final UserCredentials credentials) {

        this.client = client;
        this.credentials = credentials;
    }

    public CompletableFuture createContinuous(final String projectionName, final String query, final boolean trackEmittedStreams) {

        return CreateProjection.forContinuous(this.client, this.credentials, projectionName, query, trackEmittedStreams).execute();
    }

    public <TResult> CompletableFuture<TResult>  getResult(final String projectionName, Class<TResult> type) {
        return new GetProjectionResult<>(this.client, this.credentials, projectionName, type).execute();
    }

    public <TResult> CompletableFuture<TResult> getResult(final String projectionName,
                                                            Function<TypeFactory, JavaType> javaTypeFunction) {

        return new GetProjectionResult<TResult>(this.client, this.credentials, projectionName, javaTypeFunction).execute();
    }

}

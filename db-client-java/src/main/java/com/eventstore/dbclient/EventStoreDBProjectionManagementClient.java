package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EventStoreDBProjectionManagementClient extends EventStoreDBClientBase {
    private EventStoreDBProjectionManagementClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    public static EventStoreDBProjectionManagementClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBProjectionManagementClient(settings);
    }

    public CompletableFuture createContinuous(final String projectionName, final String query) {
        return this.createContinuous(projectionName, query, CreateContinuousProjectionOptions.get());
    }

    public CompletableFuture createContinuous(final String projectionName, final String query, CreateContinuousProjectionOptions options) {
        if (options == null)
            options = CreateContinuousProjectionOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return CreateProjection.forContinuous(this.client, projectionName, query, options).execute();
    }

    public CompletableFuture createOneTime(final String projectionName, final String query) {
        return this.createOneTime(projectionName, query, CreateOneTimeProjectionOptions.get());
    }

    public CompletableFuture createOneTime(final String projectionName, final String query, CreateOneTimeProjectionOptions options) {
        if (options == null)
            options = CreateOneTimeProjectionOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return CreateProjection.forOneTime(this.client, projectionName, query, options).execute();
    }

    public <TResult> CompletableFuture<TResult>  getResult(final String projectionName, Class<TResult> type) {
        return new GetProjectionResult<>(this.client, this.credentials, projectionName, type).execute();
    }

    public <TResult> CompletableFuture<TResult> getResult(final String projectionName,
                                                          Function<TypeFactory, JavaType> javaTypeFunction) {
        return new GetProjectionResult<TResult>(this.client, this.credentials, projectionName, javaTypeFunction).execute();
    }
}

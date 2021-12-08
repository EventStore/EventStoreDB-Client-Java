package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EventStoreDBProjectionManagementClient extends EventStoreDBClientBase {
    private EventStoreDBProjectionManagementClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    /**
     * Returns the Projection Management client based on the settings.
     * @param settings The settings to use for constructing the client.
     */
    public static EventStoreDBProjectionManagementClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBProjectionManagementClient(settings);
    }

    /**
     * Stops the projection without writing a checkpoint.
     * This can be used to disable a projection that has been faulted.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture abort(final String projectionName) {
        return this.abort(projectionName, AbortProjectionOptions.get());
    }

    /**
     * Stops the projection without writing a checkpoint.
     * This can be used to disable a projection that has been faulted.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture abort(final String projectionName, AbortProjectionOptions options) {
        return new AbortProjection(this.client, projectionName, options).execute();
    }

    /**
     * Creates a new projection in the stopped state. Enable needs to be called separately to start the projection.
     * @param projectionName Name of the projection.
     * @param query The JavaScript projection.
     */
    public CompletableFuture create(final String projectionName, final String query) {
        return this.create(projectionName, query, CreateProjectionOptions.get());
    }

    /**
     * Creates a new projection in the stopped state. Enable needs to be called separately to start the projection.
     * @param projectionName Name of the projection.
     * @param query The JavaScript projection.
     * @param options Additional options.
     */
    public CompletableFuture create(final String projectionName, final String query, CreateProjectionOptions options) {
        if (options == null)
            options = CreateProjectionOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new CreateProjection(this.client, projectionName, query, options).execute();
    }

    /**
     * Enables the projection.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture enable(final String projectionName) {
        return this.enable(projectionName, EnableProjectionOptions.get());
    }

    /**
     * Enables the projection.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture enable(final String projectionName, EnableProjectionOptions options) {
        return new EnableProjection(this.client, projectionName, options).execute();
    }

    /**
     * Deletes the projection.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture delete(final String projectionName) {
        return this.delete(projectionName, DeleteProjectionOptions.get());
    }

    /**
     * Deletes the projection.
     * @param projectionName Name of the projection.
     * @param deleteEmittedStreams Whether the streams that have been emitted by the projection should be deleted.
     *                             This only has an effect if trackEmittedStreams was enabled
     * @param deleteStateStream Whether the projection's state stream should also be deleted.
     * @param deleteCheckpointStream Whether the projection's checkpoint stream should also be deleted.
     */
    public CompletableFuture delete(final String projectionName, final boolean deleteEmittedStreams,
                                    final boolean deleteStateStream, final boolean deleteCheckpointStream) {
        DeleteProjectionOptions options = DeleteProjectionOptions.get()
                .deleteEmittedStreams(deleteEmittedStreams)
                .deleteStateStream(deleteStateStream)
                .deleteCheckpointStream(deleteCheckpointStream);

        return this.delete(projectionName, options);
    }

    /**
     * Deletes the projection.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture delete(final String projectionName, DeleteProjectionOptions options) {
        return new DeleteProjection(this.client, projectionName, options).execute();
    }

    /**
     * Disables the projection.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture disable(final String projectionName) {
        return this.disable(projectionName, DisableProjectionOptions.get());
    }

    /**
     * Disables the projection.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture disable(final String projectionName, DisableProjectionOptions options) {
        return new DisableProjection(this.client, projectionName, options).execute();
    }

    /**
     * Gets the projection's result.
     * @param projectionName Name of the projection.
     * @param type Type of the class to construct for the result.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult>  getResult(final String projectionName, Class<TResult> type) {
        return new GetProjectionResult<>(this.client, this.credentials, projectionName, type).execute();
    }

    /**
     * Gets the projection's result.
     * @param projectionName Name of the projection.
     * @param partition Name of the partition to return.
     * @param type Type of the class to construct for the result.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult>  getResult(final String projectionName, final String partition, Class<TResult> type) {
        return new GetProjectionResult<>(this.client, this.credentials, projectionName, partition, type).execute();
    }

    /**
     * Gets the projection's result.
     * @param projectionName Name of the projection.
     * @param javaTypeFunction Factory method for constructing the return type.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getResult(final String projectionName,
                                                          Function<TypeFactory, JavaType> javaTypeFunction) {
        return new GetProjectionResult<TResult>(this.client, this.credentials, projectionName, javaTypeFunction).execute();
    }

    /**
     * Gets the state of the projection.
     * @param projectionName Name of the projection.
     * @param type Type of the class to construct for the result.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getState(final String projectionName, Class<TResult> type) {
        return new GetProjectionState<>(this.client, this.credentials, projectionName, type).execute();
    }

    /**
     * Gets the state of the projection for the provided partition.
     * @param projectionName Name of the projection.
     * @param partition Name of the partition to return.
     * @param type Type of the class to construct for the result.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getState(final String projectionName, final String partition,
                                                         Class<TResult> type) {
        return new GetProjectionState<>(this.client, this.credentials, projectionName, partition, type).execute();
    }

    /**
     * Gets the state of the projection.
     * @param projectionName Name of the projection.
     * @param javaTypeFunction Factory method for constructing the return type.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getState(final String projectionName,
                                                         Function<TypeFactory, JavaType> javaTypeFunction) {
        return new GetProjectionState<TResult>(this.client, this.credentials, projectionName, javaTypeFunction).execute();
    }

    /**
     * Gets the state of the projection for the provided partition.
     * @param projectionName Name of the projection.
     * @param partition Name of the partition to return.
     * @param javaTypeFunction Factory method for constructing the return type.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getState(final String projectionName, final String partition,
                                                         Function<TypeFactory, JavaType> javaTypeFunction) {
        return new GetProjectionState<TResult>(this.client, this.credentials, projectionName, partition, javaTypeFunction).execute();
    }

    /**
     * Gets the statistics for the projection.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture<ProjectionDetails> getStatistics(final String projectionName) {
        return this.getStatistics(projectionName, GetProjectionStatisticsOptions.get());
    }

    /**
     * Gets the statistics for the projection.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture<ProjectionDetails> getStatistics(final String projectionName, GetProjectionStatisticsOptions options) {
        return new GetProjectionStatistics(this.client, projectionName, options).execute();
    }

    /**
     * Gets the projection's current status.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture<ProjectionDetails> getStatus(final String projectionName) {
        return this.getStatus(projectionName, GetProjectionStatusOptions.get());
    }

    /**
     * Gets the projection's current status.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture<ProjectionDetails> getStatus(final String projectionName, final GetProjectionStatusOptions options) {
        return new GetProjectionStatus(this.client, projectionName, options).execute();
    }

    /**
     * Lists all continuous projections.
     */
    public CompletableFuture<ListProjectionsResult> list() {
        return new ListProjections(this.client, ListProjectionsOptions.get()).execute();
    }

    /**
     * Lists all continuous projections.
     * @param options Additional options.
     */
    public CompletableFuture<ListProjectionsResult> list(ListProjectionsOptions options) {
        return new ListProjections(this.client, options).execute();
    }

    /**
     * Resets the projection, causing it to start again from the beginning of the stream/s it selects from.
     * Resetting a projection will truncate all emitted streams and re-emit all events.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture reset(final String projectionName) {
        return this.reset(projectionName, ResetProjectionOptions.get());
    }

    /**
     * Resets the projection, causing it to start again from the beginning of the stream/s it selects from.
     * Resetting a projection will truncate all emitted streams and re-emit all events.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture reset(final String projectionName, ResetProjectionOptions options) {
        return new ResetProjection(this.client, projectionName, options).execute();
    }

    /**
     * Restarts the projection subsystem. This can be used to recover from certain kinds of errors.
     */
    public CompletableFuture restartSubsystem() {
        return this.restartSubsystem(RestartProjectionSubsystemOptions.get());
    }

    /**
     * Restarts the projection subsystem. This can be used to recover from certain kinds of errors.
     * @param options Additional options.
     */
    public CompletableFuture restartSubsystem(RestartProjectionSubsystemOptions options) {
        return new RestartProjectionSubsystem(this.client, options).execute();
    }

    /**
     * Updates the projection's query and emit options.
     * @param projectionName Name of the projection.
     * @param query The JavaScript projection.
     */
    public CompletableFuture update(final String projectionName, final String query) {
        return this.update(projectionName, query, UpdateProjectionOptions.get());
    }

    /**
     * Updates the projection's query and emit options.
     * @param projectionName Name of the projection.
     * @param query The JavaScript projection.
     * @param options Additional options.
     */
    public CompletableFuture update(final String projectionName, final String query, UpdateProjectionOptions options) {
        return new UpdateProjection(this.client, projectionName, query, options).execute();
    }
}

package com.eventstore.dbclient;

import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a client to a single node. A client instance maintains a full duplex communication to EventStoreDB.
 * Many threads can use an EventStoreDB client at the same time or a single thread can make many asynchronous requests.
 */
public class EventStoreDBClient extends EventStoreDBClientBase {
    private EventStoreDBClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    /**
     * Creates a gRPC client to an EventStoreDB database.
     */
    public static EventStoreDBClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBClient(settings);
    }

    /**
     * Appends events to a given stream.
     * @param streamName stream's name.
     * @param events events to send.
     * @see WriteResult
     * @return a write result if successful.
     */
    public CompletableFuture<WriteResult> appendToStream(String streamName, EventData... events) {
        return this.appendToStream(streamName, Arrays.stream(events).iterator());
    }

    /**
     * Appends events to a given stream.
     * @param streamName stream's name.
     * @param events events to send.
     * @see WriteResult
     * @return a write result if successful.
     */
    public CompletableFuture<WriteResult> appendToStream(String streamName, Iterator<EventData> events) {
        return this.appendToStream(streamName, AppendToStreamOptions.get(), events);
    }

    /**
     * Appends events to a given stream.
     * @param streamName stream's name.
     * @param options append stream request's options.
     * @param events events to send.
     * @see WriteResult
     * @return a write result if successful.
     */
    public CompletableFuture<WriteResult> appendToStream(String streamName, AppendToStreamOptions options, EventData... events) {
        return this.appendToStream(streamName, options, Arrays.stream(events).iterator());
    }

    /**
     * Appends events to a given stream.
     * @param streamName stream's name.
     * @param options append stream request's options.
     * @param events events to send.
     * @see WriteResult
     * @return a write result if successful.
     */
    public CompletableFuture<WriteResult> appendToStream(String streamName, AppendToStreamOptions options, Iterator<EventData> events) {
        if (options == null)
            options = AppendToStreamOptions.get();

        return new AppendToStream(this.getGrpcClient(), streamName, events, options).execute();
    }

    /**
     * Sets a stream's metadata.
     * @param streamName stream's name.
     * @param metadata stream's metadata
     * @see WriteResult
     * @return a write result if successful.
     */
    public CompletableFuture<WriteResult> setStreamMetadata(String streamName, StreamMetadata metadata) {
        return setStreamMetadata(streamName, null, metadata);
    }

    /**
     * Sets a stream's metadata.
     * @param streamName stream's name.
     * @param options append stream request's options.
     * @param metadata stream's metadata
     * @see WriteResult
     * @return a write result if successful.
     */
    public CompletableFuture<WriteResult> setStreamMetadata(String streamName, AppendToStreamOptions options, StreamMetadata metadata) {
        EventData event = EventDataBuilder.json("$metadata", metadata.serialize()).build();

        return appendToStream("$$" + streamName, options, event);
     }

    /**
     * Reads events from a given stream. The reading can be done forward and backward.
     * @param streamName stream's name.
     */
    public CompletableFuture<List<ResolvedEvent>> readStream(String streamName) {
        return this.readStream(streamName, ReadStreamOptions.get());
    }

    /**
     * Reads events from a given stream. The reading can be done forward and backward.
     * @param streamName stream's name.
     * @param options read request's operations.
     */
    public CompletableFuture<List<ResolvedEvent>> readStream(String streamName, ReadStreamOptions options) {
        return readEventsFromPublisher(this.readStreamReactive(streamName, options));
    }

    /**
     * Reads events from a given stream. The reading can be done forward and backward.
     * @param streamName stream's name.
     */
    public Publisher<ResolvedEvent> readStreamReactive(String streamName) {
        return this.readStreamReactive(streamName, ReadStreamOptions.get());
    }

    /**
     * Reads events from a given stream. The reading can be done forward and backward.
     * @param streamName stream's name.
     * @param options read request's operations.
     */
    public Publisher<ResolvedEvent> readStreamReactive(String streamName, ReadStreamOptions options) {
        if (options == null)
            options = ReadStreamOptions.get();

        return new ReadStream(this.getGrpcClient(), streamName, options);
    }

    /**
     * Reads a stream's metadata.
     * @param streamName stream's name.
     * @see StreamMetadata
     */
    public CompletableFuture<StreamMetadata> getStreamMetadata(String streamName) {
        return getStreamMetadata(streamName, null);
    }

    /**
     * Reads a stream's metadata.
     * @param streamName stream's name.
     * @param options read request's operations.
     * @see StreamMetadata
     */
    public CompletableFuture<StreamMetadata> getStreamMetadata(String streamName, ReadStreamOptions options) {

        return readStream("$$" + streamName, options).thenCompose(result -> {
            RecordedEvent event = result.get(0).getOriginalEvent();
            CompletableFuture<StreamMetadata> out = new CompletableFuture<>();

            try {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> source = event.getEventDataAs(HashMap.class);

                out.complete(StreamMetadata.deserialize(source));
            } catch (Throwable e) {
                out.completeExceptionally(e);
            }

            return out;
        }).exceptionally(e -> {
            if (e.getCause() instanceof StreamNotFoundException) {
                return new StreamMetadata();
            }

            throw new RuntimeException(e);
        });
    }

    /**
     * Reads events from the $all stream. The reading can be done forward and backward.
     */
    public CompletableFuture<List<ResolvedEvent>> readAll() {
        return this.readAll(ReadAllOptions.get());
    }

    /**
     * Reads events from the $all stream. The reading can be done forward and backward.
     * @param options options of the read $all request.
     */
    public CompletableFuture<List<ResolvedEvent>> readAll(ReadAllOptions options) {
        return readEventsFromPublisher(this.readAllReactive(options));
    }

    /**
     * Reads events from the $all stream. The reading can be done forward and backward.
     */
    public Publisher<ResolvedEvent> readAllReactive() {
        return this.readAllReactive(ReadAllOptions.get());
    }

    /**
     * Reads events from the $all stream. The reading can be done forward and backward.
     * @param options options of the read $all request.
     */
    public Publisher<ResolvedEvent> readAllReactive(ReadAllOptions options) {
        if (options == null)
            options = ReadAllOptions.get();

        return new ReadAll(this.getGrpcClient(), options);
    }

    /**
     * Subscribes to a given stream. This kind of subscription specifies a starting point (by default, the beginning of
     * a stream). This subscription will fetch all events until the end of the stream. Then, the subscription dispatches
     * subsequently written events.
     *
     * For example, if a starting point of 50 is specified when a stream has 100 events. the subscriber can expect to
     * see events 51 through 100, and then any events subsequently written until such time as the subscription is
     * dropped or closed.
     * @param streamName stream's name.
     * @param listener consumes a subscription's events.
     * @return a subscription handle.
     */
    public CompletableFuture<Subscription> subscribeToStream(String streamName, SubscriptionListener listener) {
        return this.subscribeToStream(streamName, listener, SubscribeToStreamOptions.get());
    }

    /**
     * Subscribes to a given stream. This kind of subscription specifies a starting point (by default, the beginning of
     * a stream). This subscription will fetch all events until the end of the stream. Then, the subscription dispatches
     * subsequently written events.
     *
     * For example, if a starting point of 50 is specified when a stream has 100 events. the subscriber can expect to
     * see events 51 through 100, and then any events subsequently written until such time as the subscription is
     * dropped or closed.
     * @param streamName stream's name.
     * @param listener consumes a subscription's events.
     * @param options a subscription request's options.
     * @return a subscription handle.
     */
    public CompletableFuture<Subscription> subscribeToStream(String streamName, SubscriptionListener listener, SubscribeToStreamOptions options) {
        if (options == null)
            options = SubscribeToStreamOptions.get();

        return new SubscribeToStream(this.getGrpcClient(), streamName, listener, options).execute();
    }

    /**
     * Subscribes to $all. This kind of subscription specifies a starting point (by default, the beginning of
     * a stream). This subscription will fetch all events until the end of the stream. Then, the subscription dispatches
     * subsequently written events.
     *
     * @param listener consumes a subscription's events.
     * @return a subscription handle.
     */
    public CompletableFuture<Subscription> subscribeToAll(SubscriptionListener listener) {
        return this.subscribeToAll(listener, SubscribeToAllOptions.get());
    }

    /**
     * Subscribes to $all. This kind of subscription specifies a starting point (by default, the beginning of
     * a stream). This subscription will fetch all events until the end of the stream. Then, the subscription dispatches
     * subsequently written events.
     *
     * @param listener consumes a subscription's events.
     * @param options subscription to $all request's options.
     * @return a subscription handle.
     */
    public CompletableFuture<Subscription> subscribeToAll(SubscriptionListener listener, SubscribeToAllOptions options) {
        if (options == null)
            options = SubscribeToAllOptions.get();

        return new SubscribeToAll(this.getGrpcClient(), listener, options).execute();
    }

    /**
     * Soft deletes a given stream.
     * <p>
     * Makes use of Truncate before, When a stream is deleted, its Truncate before is set to the stream's current last
     * event number. When a soft deleted stream is read, the read will return a <i>StreamNotFound</i> error. After
     * deleting the stream, you are able to write to it again, continuing from where it left off.
     * </p>
     * @param streamName stream's name
     * @see DeleteResult
     * @return if successful, delete result.
     */
    public CompletableFuture<DeleteResult> deleteStream(String streamName) {
        return this.deleteStream(streamName, DeleteStreamOptions.get());
    }

    /**
     * Soft deletes a given stream.
     * <p>
     * Makes use of Truncate before, When a stream is deleted, its Truncate before is set to the stream's current last
     * event number. When a soft deleted stream is read, the read will return a <i>StreamNotFound</i> error. After
     * deleting the stream, you are able to write to it again, continuing from where it left off.
     * </p>
     * @param streamName stream's name
     * @param options delete stream request's options.
     * @see DeleteResult
     * @return if successful, delete result.
     */
    public CompletableFuture<DeleteResult> deleteStream(String streamName, DeleteStreamOptions options) {
        if (options == null)
            options = DeleteStreamOptions.get();

        return new DeleteStream(this.getGrpcClient(), streamName, true, options).execute();
    }

    /**
     * Hard deletes a given stream.
     * <p>
     * A hard delete writes a tombstone event to the stream, permanently deleting it. The stream cannot be recreated or
     * written to again. Tombstone events are written with the event's type '$streamDeleted'. When a hard deleted stream
     * is read, the read will return a <i>StreamDeleted</i> error.
     * </p>
     * @param streamName a stream's name.
     * @see DeleteResult
     * @return if successful, delete result.
     */
    public CompletableFuture<DeleteResult> tombstoneStream(String streamName) {
        return this.deleteStream(streamName, DeleteStreamOptions.get());
    }

    /**
     * Hard deletes a given stream.
     * <p>
     * A hard delete writes a tombstone event to the stream, permanently deleting it. The stream cannot be recreated or
     * written to again. Tombstone events are written with the event's type '$streamDeleted'. When a hard deleted stream
     * is read, the read will return a <i>StreamDeleted</i> error.
     * </p>
     * @param streamName a stream's name.
     * @param options delete stream request's options.
     * @see DeleteResult
     * @return if successful, delete result.
     */
    public CompletableFuture<DeleteResult> tombstoneStrSeam(String streamName, DeleteStreamOptions options) {
        if (options == null)
            options = DeleteStreamOptions.get();

        return new DeleteStream(this.getGrpcClient(), streamName, false, options).execute();
    }

    private static CompletableFuture<List<ResolvedEvent>> readEventsFromPublisher(Publisher<ResolvedEvent> eventPublisher) {
        CompletableFuture<List<ResolvedEvent>> future = new CompletableFuture<>();
        List<ResolvedEvent> events = new LinkedList<>();
        eventPublisher.subscribe(new ReadSubscriber() {
            @Override
            public void onEvent(ResolvedEvent resolvedEvent) {
                events.add(resolvedEvent);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(events);
            }
        });
        return future;
    }
}

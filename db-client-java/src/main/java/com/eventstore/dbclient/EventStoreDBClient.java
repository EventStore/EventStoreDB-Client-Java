package com.eventstore.dbclient;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EventStoreDBClient extends EventStoreDBClientBase {
    private EventStoreDBClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    public static EventStoreDBClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBClient(settings);
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, EventData... events) {
        return this.appendToStream(streamName, Arrays.stream(events).iterator());
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, Iterator<EventData> events) {
        return this.appendToStream(streamName, AppendToStreamOptions.get(), events);
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, AppendToStreamOptions options, EventData... events) {
        return this.appendToStream(streamName, options, Arrays.stream(events).iterator());
    }

    public CompletableFuture<WriteResult> appendToStream(String streamName, AppendToStreamOptions options, Iterator<EventData> events) {
        if (options == null)
            options = AppendToStreamOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new AppendToStream(this.client, streamName, events, options).execute();
    }

    public CompletableFuture<WriteResult> setStreamMetadata(String streamName, StreamMetadata metadata) {
        return setStreamMetadata(streamName, null, metadata);
    }

    public CompletableFuture<WriteResult> setStreamMetadata(String streamName, AppendToStreamOptions options, StreamMetadata metadata) {
        EventData event = EventDataBuilder.json("$metadata", metadata.serialize()).build();

        return appendToStream("$$" + streamName, options, event);
     }

    public CompletableFuture<ReadResult> readStream(String streamName) {
        return this.readStream(streamName, Long.MAX_VALUE, ReadStreamOptions.get());
    }

    public CompletableFuture<ReadResult> readStream(String streamName, long maxCount) {
        return this.readStream(streamName, maxCount, ReadStreamOptions.get());
    }

    public CompletableFuture<ReadResult> readStream(String streamName, ReadStreamOptions options) {
        return this.readStream(streamName, Long.MAX_VALUE, options);
    }

    public CompletableFuture<ReadResult> readStream(String streamName, long maxCount, ReadStreamOptions options) {
        Publisher<ReadMessage> publisher = readStreamReactive(streamName, maxCount, options);
        return readEventsFromPublisher(publisher);
    }

    public Publisher<ReadMessage> readStreamReactive(String streamName) {
        return this.readStreamReactive(streamName, Long.MAX_VALUE, ReadStreamOptions.get());
    }

    public Publisher<ReadMessage> readStreamReactive(String streamName, ReadStreamOptions options) {
        return this.readStreamReactive(streamName, Long.MAX_VALUE, options);
    }

    public Publisher<ReadMessage> readStreamReactive(String streamName, long maxCount, ReadStreamOptions options) {
        if (options == null)
            options = ReadStreamOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new ReadStream(this.client, streamName, maxCount, options);
    }

    private static <A, B> Publisher<B> publisherMap(final Publisher<A> parent, Function<A, B> fun) {
        return sub -> {
            parent.subscribe(new Subscriber<A>() {
                @Override
                public void onSubscribe(org.reactivestreams.Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(A a) {
                    sub.onNext(fun.apply(a));
                }

                @Override
                public void onError(Throwable t) {
                    sub.onError(t);
                }

                @Override
                public void onComplete() {
                    sub.onComplete();
                }
            });
        };
    }

    public CompletableFuture<StreamMetadata> getStreamMetadata(String streamName) {
        return getStreamMetadata(streamName, null);
    }

    public CompletableFuture<StreamMetadata> getStreamMetadata(String streamName, ReadStreamOptions options) {

        return readStream("$$" + streamName, options).thenCompose(result -> {
            RecordedEvent event = result.getEvents().get(0).getOriginalEvent();
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

    public CompletableFuture<ReadResult> readAll() {
        return this.readAll(Long.MAX_VALUE, ReadAllOptions.get());
    }

    public CompletableFuture<ReadResult> readAll(long maxCount) {
        return this.readAll(maxCount, ReadAllOptions.get());
    }

    public CompletableFuture<ReadResult> readAll(ReadAllOptions options) {
        return this.readAll(Long.MAX_VALUE, options);
    }

    public CompletableFuture<ReadResult> readAll(long maxCount, ReadAllOptions options) {
        return readEventsFromPublisher(this.readAllReactive(maxCount, options));
    }

    public Publisher<ReadMessage> readAllReactive() {
        return this.readAllReactive(Long.MAX_VALUE, ReadAllOptions.get());
    }

    public Publisher<ReadMessage> readAllReactive(long maxCount) {
        return this.readAllReactive(maxCount, ReadAllOptions.get());
    }

    public Publisher<ReadMessage> readAllReactive(ReadAllOptions options) {
        return this.readAllReactive(Long.MAX_VALUE, options);
    }

    public Publisher<ReadMessage> readAllReactive(long maxCount, ReadAllOptions options) {
        if (options == null)
            options = ReadAllOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new ReadAll(this.client, maxCount, options);
    }

    public CompletableFuture<Subscription> subscribeToStream(String streamName, SubscriptionListener listener) {
        return this.subscribeToStream(streamName, listener, SubscribeToStreamOptions.get());
    }

    public CompletableFuture<Subscription> subscribeToStream(String streamName, SubscriptionListener listener, SubscribeToStreamOptions options) {
        if (options == null)
            options = SubscribeToStreamOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new SubscribeToStream(this.client, streamName, listener, options).execute();
    }

    public CompletableFuture<Subscription> subscribeToAll(SubscriptionListener listener) {
        return this.subscribeToAll(listener, SubscribeToAllOptions.get());
    }

    public CompletableFuture<Subscription> subscribeToAll(SubscriptionListener listener, SubscribeToAllOptions options) {
        if (options == null)
            options = SubscribeToAllOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new SubscribeToAll(this.client, listener, options).execute();
    }

    public CompletableFuture<DeleteResult> deleteStream(String streamName) {
        return this.deleteStream(streamName, DeleteStreamOptions.get());
    }

    public CompletableFuture<DeleteResult> deleteStream(String streamName, DeleteStreamOptions options) {
        if (options == null)
            options = DeleteStreamOptions.get();

        if (!options.hasUserCredentials())
            options.authenticated(this.credentials);

        return new DeleteStream(this.client, streamName, options).execute();
    }

    private static CompletableFuture<ReadResult> readEventsFromPublisher(Publisher<ReadMessage> eventPublisher) {
        CompletableFuture<ReadResult> future = new CompletableFuture<>();
        List<ResolvedEvent> events = new LinkedList<>();

        eventPublisher.subscribe(new ReadSubscriber() {
            long firstStreamPosition = 0;
            long lastStreamPosition = 0;
            Position lastAllStreamPosition = null;

            @Override
            public void onEvent(ReadMessage e) {
                if (e.hasFirstStreamPosition()) {
                    firstStreamPosition = e.getFirstStreamPosition();
                    return;
                }

                if (e.hasLastStreamPosition()) {
                    lastStreamPosition = e.getLastStreamPosition();
                    return;
                }

                if (e.hasLastAllPosition()) {
                    lastAllStreamPosition = e.getLastAllPosition();
                    return;
                }

                if (e.hasEvent())
                    events.add(e.getEvent());
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(new ReadResult(events, firstStreamPosition, lastStreamPosition, lastAllStreamPosition));
            }
        });
        return future;
    }
}

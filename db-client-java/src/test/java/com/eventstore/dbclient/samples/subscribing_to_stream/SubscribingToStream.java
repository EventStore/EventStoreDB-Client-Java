package com.eventstore.dbclient.samples.subscribing_to_stream;

import com.eventstore.dbclient.*;

import static org.junit.Assert.fail;

public class SubscribingToStream {
    private static void subscribeToStream(EventStoreDBClient client) {
        // region subscribe-to-stream
        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getStreamRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
                HandleEvent(event);
            }
        };
        client.subscribeToStream("some-stream", listener);
        // endregion subscribe-to-stream

        // region subscribe-to-stream-from-position
        client.subscribeToStream(
                "some-stream",
                listener,
                SubscribeToStreamOptions.get()
                        .fromRevision(20)
        );
        // endregion subscribe-to-stream-from-position

        // region subscribe-to-stream-live
        client.subscribeToStream(
                "some-stream",
                listener,
                SubscribeToStreamOptions.get()
                        .fromEnd()
        );
        // endregion subscribe-to-stream-live

        // region subscribe-to-stream-resolving-linktos
        client.subscribeToStream(
                "$et-myEventType",
                listener,
                SubscribeToStreamOptions.get()
                        .fromStart()
                        .resolveLinkTos()
        );
        // endregion subscribe-to-stream-resolving-linktos

        // region subscribe-to-stream-subscription-dropped
        final StreamRevision[] checkpoint = {StreamRevision.START};

        client.subscribeToStream(
                "some-stream",
                new SubscriptionListener() {
                    @Override
                    public void onEvent(Subscription subscription, ResolvedEvent event) {
                        HandleEvent(event);
                        checkpoint[0] = event.getOriginalEvent().getStreamRevision();
                    }

                    @Override
                    public void onError(Subscription subscription, Throwable throwable) {
                        System.out.println("Subscription was dropped due to " + throwable.getMessage());
                        Resubscribe(checkpoint[0]);
                    }
                },
                SubscribeToStreamOptions.get()
                        .fromRevision(checkpoint[0])
        );
        // endregion subscribe-to-stream-subscription-dropped
    }

    private static void subscribeToAll(EventStoreDBClient client) {
        // region subscribe-to-all
        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getStreamRevision().getValueUnsigned()
                        + "@" + event.getOriginalEvent().getStreamId());
                HandleEvent(event);
            }
        };
        client.subscribeToAll(listener);
        // endregion subscribe-to-all

        // region subscribe-to-all-from-position
        client.subscribeToAll(
                listener,
                SubscribeToAllOptions.get()
                        .fromPosition(new Position(1056, 1056))
        );
        // endregion subscribe-to-all-from-position

        // region subscribe-to-all-live
        client.subscribeToAll(
                listener,
                SubscribeToAllOptions.get()
                        .fromEnd()
        );
        // endregion subscribe-to-all-live

        // region subscribe-to-all-subscription-dropped
        final Position[] checkpoint = {Position.START};

        client.subscribeToAll(
                new SubscriptionListener() {
                    @Override
                    public void onEvent(Subscription subscription, ResolvedEvent event) {
                        HandleEvent(event);
                        checkpoint[0] = event.getOriginalEvent().getPosition();
                    }

                    @Override
                    public void onError(Subscription subscription, Throwable throwable) {
                        System.out.println("Subscription was dropped due to " + throwable.getMessage());
                        Resubscribe(checkpoint[0]);
                    }
                },
                SubscribeToAllOptions.get()
                        .fromPosition(checkpoint[0])
        );
        // endregion subscribe-to-all-subscription-dropped
    }

    private static void SubscribeToFiltered(EventStoreDBClient client) {
        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getStreamRevision().getValueUnsigned()
                        + "@" + event.getOriginalEvent().getStreamId());
                HandleEvent(event);
            }
        };
        // region stream-prefix-filtered-subscription
        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withStreamNamePrefix("test-")
                .build();

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .filter(filter);

        client.subscribeToAll(
                listener,
                options);
        // endregion stream-prefix-filtered-subscription

        // region stream-regex-filtered-subscription
        SubscriptionFilter regexStreamFilter = SubscriptionFilter.newBuilder()
                .withStreamNameRegularExpression("/invoice-\\d\\d\\d/g")
                .build();
        // endregion stream-regex-filtered-subscription
    }

    private static void OverridingUserCredentials(EventStoreDBClient client) {
        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getStreamRevision().getValueUnsigned()
                        + "@" + event.getOriginalEvent().getStreamId());
                HandleEvent(event);
            }
        };
        // region overriding-user-credentials
        UserCredentials credentials = new UserCredentials("admin", "changeit");

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .authenticated(credentials);

        client.subscribeToAll(
                listener,
                options);
        // endregion overriding-user-credentials
    }

    private static void HandleEvent(ResolvedEvent event) {
    }

    private static void Resubscribe(StreamRevision checkpoint) {
    }

    private static void Resubscribe(Position checkpoint) {
    }
}

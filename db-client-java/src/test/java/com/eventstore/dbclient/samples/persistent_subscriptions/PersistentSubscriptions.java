package com.eventstore.dbclient.samples.persistent_subscriptions;

import com.eventstore.dbclient.*;

public class PersistentSubscriptions {
    public static void createPersistentSubscription(EventStoreDBPersistentSubscriptionsClient client) {
        // region create-persistent-subscription-to-stream
        client.create(
            "test-stream",
            "subscription-group",
            PersistentSubscriptionSettings.builder()
                .fromStart()
                .build());
        // endregion create-persistent-subscription-to-stream
    }

    public static void connectToPersistentSubscriptionToStream(EventStoreDBPersistentSubscriptionsClient client) {
        // region subscribe-to-persistent-subscription-to-stream
        client.subscribe(
            "test-stream",
            "subscription-group",
            new PersistentSubscriptionListener() {
                @Override
                public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
                    System.out.println("Received event"
                        + event.getOriginalEvent().getStreamRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
                }

                @Override
                public void onError(PersistentSubscription subscription, Throwable throwable) {
                    System.out.println("Subscription was dropped due to " + throwable.getMessage());
                }

                @Override
                public void onCancelled(PersistentSubscription subscription) {
                    System.out.println("Subscription is cancelled");
                }
            });
        // endregion subscribe-to-persistent-subscription-to-stream
    }



    public static void connectToPersistentSubscriptionToStreamWithManualAcks(EventStoreDBPersistentSubscriptionsClient client) {
        // region subscribe-to-persistent-subscription-with-manual-acks
        client.subscribe(
                "test-stream",
                "subscription-group",
                new PersistentSubscriptionListener() {
                    @Override
                    public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
                        try {
                            System.out.println("Received event"
                                    + event.getOriginalEvent().getStreamRevision()
                                    + "@" + event.getOriginalEvent().getStreamId());
                            subscription.ack(event);
                        }
                        catch (Exception ex) {
                            subscription.nack(NackAction.Park, ex.getMessage(), event);
                        }
                    }
                });
        // endregion subscribe-to-persistent-subscription-with-manual-acks
    }

    public static void createPersistentSubscriptionToAll(EventStoreDBPersistentSubscriptionsClient client) {
        // region create-persistent-subscription-to-all
        client.createToAll(
            "subscription-group",
            PersistentSubscriptionToAllSettings.builder()
                .fromStart()
                .build());
        // endregion create-persistent-subscription-to-all
    }

    public static void connectToPersistentSubscriptionToAll(EventStoreDBPersistentSubscriptionsClient client) {
        // region subscribe-to-persistent-subscription-to-all
        client.subscribeToAll(
            "subscription-group",
            new PersistentSubscriptionListener() {
                @Override
                public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
                    try {
                        System.out.println("Received event"
                            + event.getOriginalEvent().getStreamRevision()
                            + "@" + event.getOriginalEvent().getStreamId());
                        subscription.ack(event);
                    }
                    catch (Exception ex) {
                        subscription.nack(NackAction.Park, ex.getMessage(), event);
                    }
                }

                @Override
                public void onError(PersistentSubscription subscription, Throwable throwable) {
                    System.out.println("Subscription was dropped due to " + throwable.getMessage());
                }

                @Override
                public void onCancelled(PersistentSubscription subscription) {
                    System.out.println("Subscription is cancelled");
                }
            });
        // endregion subscribe-to-persistent-subscription-to-all
    }

    public static void updatePersistentSubscription(EventStoreDBPersistentSubscriptionsClient client) {
        // region update-persistent-subscription
        client.update(
            "test-stream",
            "subscription-group",
            PersistentSubscriptionSettings.builder()
                .resolveLinkTos()
                .checkPointLowerBound(20)
                .build());
        // endregion update-persistent-subscription
    }

    public static void deletePersistentSubscription(EventStoreDBPersistentSubscriptionsClient client) {
        // region delete-persistent-subscription
        client.delete(
            "test-stream",
            "subscription-group");
        // endregion delete-persistent-subscription
    }
}

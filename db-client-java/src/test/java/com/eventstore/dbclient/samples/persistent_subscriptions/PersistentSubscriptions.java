package com.eventstore.dbclient.samples.persistent_subscriptions;

import com.eventstore.dbclient.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class PersistentSubscriptions {
    public static void createPersistentSubscription(EventStoreDBPersistentSubscriptionsClient client) {
        // region create-persistent-subscription-to-stream
        client.createToStream(
            "test-stream",
            "subscription-group",
            CreatePersistentSubscriptionToStreamOptions.get()
                .fromStart());
        // endregion create-persistent-subscription-to-stream
    }

    public static void connectToPersistentSubscriptionToStream(EventStoreDBPersistentSubscriptionsClient client) {
        // region subscribe-to-persistent-subscription-to-stream
        client.subscribeToStream(
            "test-stream",
            "subscription-group",
            new PersistentSubscriptionListener() {
                @Override
                public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                    System.out.println("Received event"
                        + event.getOriginalEvent().getRevision()
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
        client.subscribeToStream(
                "test-stream",
                "subscription-group",
                new PersistentSubscriptionListener() {
                    @Override
                    public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                        try {
                            System.out.println("Received event"
                                    + event.getOriginalEvent().getRevision()
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
            CreatePersistentSubscriptionToAllOptions.get()
                .fromStart());
        // endregion create-persistent-subscription-to-all
    }

    public static void connectToPersistentSubscriptionToAll(EventStoreDBPersistentSubscriptionsClient client) {
        // region subscribe-to-persistent-subscription-to-all
        client.subscribeToAll(
            "subscription-group",
            new PersistentSubscriptionListener() {
                @Override
                public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                    try {
                        System.out.println("Received event"
                            + event.getOriginalEvent().getRevision()
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
        client.updateToStream(
            "test-stream",
            "subscription-group",
            UpdatePersistentSubscriptionToStreamOptions.get()
                .resolveLinkTos()
                .checkpointLowerBound(20));
        // endregion update-persistent-subscription
    }

    public static void deletePersistentSubscription(EventStoreDBPersistentSubscriptionsClient client) {
        // region delete-persistent-subscription
        client.deleteToStream(
            "test-stream",
            "subscription-group");
        // endregion delete-persistent-subscription
    }

    public static void getPersistentSubscriptionToStreamInfo(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region get-persistent-subscription-to-stream-info
        Optional<PersistentSubscriptionToStreamInfo> result = client.getInfoToStream("test-stream", "subscription-group").get();
        if (result.isPresent()) {
            PersistentSubscriptionInfo info = result.get();

           System.out.println("GroupName: " + info.getGroupName() + ", EventStreamId: " + info.getEventSource() + ", Status " + info.getStatus());
        }
        // #endregion get-persistent-subscription-to-stream-info
    }

    public static void getPersistentSubscriptionToAllInfo(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region get-persistent-subscription-to-all-info
        Optional<PersistentSubscriptionToAllInfo> result = client.getInfoToAll( "subscription-group").get();
        if (result.isPresent()) {
            PersistentSubscriptionInfo info = result.get();

            System.out.println("GroupName: " + info.getGroupName() + ", EventStreamId: " + info.getEventSource() + ", Status " + info.getStatus());
        }
        // #endregion get-persistent-subscription-to-all-info
    }

    public static void replayParkedToStream(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region replay-parked-of-persistent-subscription-to-stream
        ReplayParkedMessagesOptions options = ReplayParkedMessagesOptions.get().stopAt(10);
        client.replayParkedMessagesToStream("test-stream", "subscription-group", options).get();
        // #endregion replay-parked-of-persistent-subscription-to-stream
    }

    public static void replayParkedToAll(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region replay-parked-of-persistent-subscription-to-all
        ReplayParkedMessagesOptions options = ReplayParkedMessagesOptions.get().stopAt(10);
        client.replayParkedMessagesToAll("subscription-group", options).get();
        // #endregion replay-parked-of-persistent-subscription-to-all
    }

    public static void listPersistentSubscriptionToStream(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region list-persistent-subscriptions-to-stream
        List<PersistentSubscriptionToStreamInfo> subscriptions = client.listToStream("test-stream").get();

        for (PersistentSubscriptionInfo info : subscriptions) {
            System.out.println("GroupName: " + info.getGroupName() + ", EventStreamId: " + info.getEventSource() + ", Status " + info.getStatus());
        }
        // #endregion list-persistent-subscriptions-to-stream
    }

    public static void listPersistentSubscriptionToAll(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region list-persistent-subscriptions-to-all
        List<PersistentSubscriptionToAllInfo> subscriptions = client.listToAll().get();

        for (PersistentSubscriptionInfo info : subscriptions) {
            System.out.println("GroupName: " + info.getGroupName() + ", EventStreamId: " + info.getEventSource() + ", Status " + info.getStatus());
        }
        // #endregion list-persistent-subscriptions-to-all
    }

    public static void restartPersistentSubscriptionSubsystem(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region restart-persistent-subscription-subsystem
        client.restartSubsystem().get();
        // #endregion restart-persistent-subscription-subsystem
    }
}

package com.eventstore.dbclient.samples.persistent_subscriptions;

import com.eventstore.dbclient.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class PersistentSubscriptions {
    public static void createPersistentSubscription(EventStoreDBPersistentSubscriptionsClient client) {
        // region create-persistent-subscription-to-stream
        client.create(
            "test-stream",
            "subscription-group",
            PersistentSubscriptionSettings.builder()
                .fromStart()
                .build());
        // region create-persistent-subscription-to-stream
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
        // region subscribe-to-persistent-subscription-to-stream
    }

    public static void createPersistentSubscriptionToAll(EventStoreDBPersistentSubscriptionsClient client) {
        // region create-persistent-subscription-to-all
        client.createToAll(
            "subscription-group",
            PersistentSubscriptionToAllSettings.builder()
                .fromStart()
                .build());
        // region create-persistent-subscription-to-all
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
        // region subscribe-to-persistent-subscription-to-all
    }

    public static void updatePersistentSubscription(EventStoreDBPersistentSubscriptionsClient client) {
        // region update-persistent-subscription-to-stream
        client.update(
            "test-stream",
            "subscription-group",
            PersistentSubscriptionSettings.builder()
                .resolveLinkTos()
                .checkPointLowerBound(20)
                .build());
        // region update-persistent-subscription-to-stream
    }

    public static void deletePersistentSubscription(EventStoreDBPersistentSubscriptionsClient client) {
        // region delete-persistent-subscription-to-stream
        client.delete(
            "test-stream",
            "subscription-group");
        // region delete-persistent-subscription-to-stream
    }

    public static void getPersistentSubscriptionToStreamInfo(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region get-persistent-subscription-to-stream-info
        Optional<PersistentSubscriptionInfo> result = client.getInfo("test-stream", "subscription-group").get();
        if (result.isPresent()) {
            PersistentSubscriptionInfo info = result.get();

           System.out.println("GroupName: " + info.getGroupName() + ", EventStreamId: " + info.getEventStreamId() + ", Status " + info.getStatus());
        }
        // #endregion get-persistent-subscription-to-stream-info
    }

    public static void getPersistentSubscriptionToAllInfo(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region get-persistent-subscription-to-all-info
        Optional<PersistentSubscriptionInfo> result = client.getInfoToAll( "subscription-group").get();
        if (result.isPresent()) {
            PersistentSubscriptionInfo info = result.get();

            System.out.println("GroupName: " + info.getGroupName() + ", EventStreamId: " + info.getEventStreamId() + ", Status " + info.getStatus());
        }
        // #endregion get-persistent-subscription-to-all-info
    }

    public static void replayParkedToStream(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region replay-parked-of-persistent-subscription-to-stream
        ReplayParkedMessagesOptions options = ReplayParkedMessagesOptions.get().stopAt(10);
        client.replayParkedMessages("test-stream", "subscription-group", options).get();
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
        List<PersistentSubscriptionInfo> subscriptions = client.listForStream("test-stream").get();

        for (PersistentSubscriptionInfo info : subscriptions) {
            System.out.println("GroupName: " + info.getGroupName() + ", EventStreamId: " + info.getEventStreamId() + ", Status " + info.getStatus());
        }
        // #endregion list-persistent-subscriptions-to-stream
    }

    public static void listPersistentSubscriptionToAll(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region list-persistent-subscriptions-to-all
        List<PersistentSubscriptionInfo> subscriptions = client.listToAll().get();

        for (PersistentSubscriptionInfo info : subscriptions) {
            System.out.println("GroupName: " + info.getGroupName() + ", EventStreamId: " + info.getEventStreamId() + ", Status " + info.getStatus());
        }
        // #endregion list-persistent-subscriptions-to-all
    }

    public static void restartPersistentSubscriptionSubsystem(EventStoreDBPersistentSubscriptionsClient client) throws ExecutionException, InterruptedException {
        // #region restart-persistent-subscription-subsystem
        client.restartSubsystem().get();
        // #endregion restart-persistent-subscription-subsystem
    }
}

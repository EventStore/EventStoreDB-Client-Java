package com.eventstore.dbclient;

/**
 * Used when consuming a persistent subscription. This class consists of methods representing all events of a persistent
 * subscription's lifecycle.
 */
public abstract class PersistentSubscriptionListener {
    /**
     * Called when EventStoreDB sends an event to the persistent subscription.
     * @param subscription handle to the persistent subscription.
     * @param event a resolved event.
     */
    public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
    }

    /**
     * Called when an exception was raised when processing an event.
     * @param subscription handle to the persistent subscription.
     * @param throwable an exception.
     */
    public void onError(PersistentSubscription subscription, Throwable throwable) {
    }

    /**
     * Called when the subscription is cancelled or dropped.
     * @param subscription handle to the persistent subscription.
     */
    public void onCancelled(PersistentSubscription subscription) {
    }
}
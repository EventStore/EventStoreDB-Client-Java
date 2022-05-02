package com.eventstore.dbclient;

/**
 * Listener used to handle persistent subscription notifications raised throughout its lifecycle.
 */
public abstract class PersistentSubscriptionListener {
    /**
     * Called when EventStoreDB sends an event to the persistent subscription.
     * @param subscription handle to the persistent subscription.
     * @param retryCount how many times the event was retried.
     * @param event a resolved event.
     */
    public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
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

    public void onConfirmation(PersistentSubscription subscription) {
    }
}
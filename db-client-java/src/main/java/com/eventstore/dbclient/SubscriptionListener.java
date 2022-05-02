package com.eventstore.dbclient;

/**
 * Used when consuming a subscription. This class consists of methods representing all events of a
 * subscription's lifecycle.
 */
public abstract class SubscriptionListener {
    /**
     * Called when EventStoreDB sends an event to the subscription.
     * @param subscription handle to the subscription.
     * @param event a resolved event.
     */
    public void onEvent(Subscription subscription, ResolvedEvent event) {
    }

    /**
     * Called when an exception was raised when processing an event.
     * @param subscription handle to the subscription.
     * @param throwable an exception.
     */
    public void onError(Subscription subscription, Throwable throwable) {
    }

    /**
     * Called when the subscription is cancelled or dropped.
     * @param subscription handle to the subscription.
     */
    public void onCancelled(Subscription subscription) {
    }
}

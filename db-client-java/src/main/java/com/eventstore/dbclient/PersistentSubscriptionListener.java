package com.eventstore.dbclient;

public abstract class PersistentSubscriptionListener {
    public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
    }

    public void onError(PersistentSubscription subscription, Throwable throwable) {
    }

    public void onCancelled(PersistentSubscription subscription) {
    }

    public void onConfirmation(PersistentSubscription subscription) {
    }
}
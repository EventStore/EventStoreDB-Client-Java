package com.eventstore.dbclient;

public abstract class PersistentSubscriptionListener {
    public void onEvent(PersistentSubscription subscription, ResolvedEvent event) {
    }

    public void onError(PersistentSubscription subscription, Throwable throwable) {
    }

    public void onCancelled(PersistentSubscription subscription) {
    }
}
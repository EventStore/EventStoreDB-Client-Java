package com.eventstore.dbclient;

public abstract class SubscriptionListener {
    public void onEvent(Subscription subscription, ResolvedEvent event) {
    }

    public void onError(Subscription subscription, Throwable throwable) {
    }

    public void onCancelled(Subscription subscription) {
    }

    public void onConfirmation(Subscription subscription) {

    }
}

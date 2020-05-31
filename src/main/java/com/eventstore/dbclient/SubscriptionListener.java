package com.eventstore.dbclient;

public abstract class SubscriptionListener {
    void onEvent(Subscription subscription, ResolvedEvent event) {
    }

    void onError(Subscription subscription, Throwable throwable) {
    }

    void onCancelled(Subscription subscription) {
    }
}

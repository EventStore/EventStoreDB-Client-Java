package com.eventstore.dbclient;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

abstract class ReadSubscriber implements Subscriber<ResolvedEvent> {

    private Subscription subscription;

    @Override
    public final void onSubscribe(Subscription s) {
        this.subscription = s;
        request(Long.MAX_VALUE);
    }

    public final void request(long n) {
        this.subscription.request(n);
    }

    @Override
    public final void onNext(ResolvedEvent resolvedEvent) {
        onEvent(resolvedEvent);
    }

    public abstract void onEvent(ResolvedEvent resolvedEvent);
}

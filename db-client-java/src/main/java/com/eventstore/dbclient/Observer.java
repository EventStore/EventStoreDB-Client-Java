package com.eventstore.dbclient;

import java.util.concurrent.CompletableFuture;

public class AsyncObserver<A, R> {
    private final CompletableFuture<R> future = new CompletableFuture<>();
    private final Function2<A, R, R> iteratee;
    private R seed;
    private boolean completed = false;

    public AsyncObserver(R seed, Function2<A, R, R> iteratee) {
       this.iteratee = iteratee;
       this.seed = seed;
    }

    public void onNext(A value) {
        if (completed)
            return;

        try {
            seed = iteratee.apply(value, seed);
        } catch (Throwable t) {
            completed = true;
            onError(t);
        }
    }

    public void onError(Throwable t) {
        if (completed)
            return;

        completed = true;
        future.completeExceptionally(t);
    }

    public void onComplete() {
        if (completed)
            return;

        completed = true;
        future.complete(seed);
    }

    public CompletableFuture<R> getFuture() {
        return future;
    }
}

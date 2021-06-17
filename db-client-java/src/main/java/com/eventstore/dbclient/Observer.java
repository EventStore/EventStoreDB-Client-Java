package com.eventstore.dbclient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class Observer<A, R> {
    private final CompletableFuture<R> future = new CompletableFuture<>();
    private final Fold<A, R> iteratee;
    private Object seed;
    private boolean completed = false;

    private Observer(Fold<A, R> iteratee) {
        this.iteratee = iteratee;
        this.seed = iteratee.getBegin();
    }

    public void onNext(A value) {
        if (completed)
            return;

        try {
            seed = iteratee.getStep().apply(value, seed);
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
        future.complete(iteratee.getDone().apply(seed));
    }

    public CompletableFuture<R> getFuture() {
        return future;
    }

    public boolean isCompleted() {
        return completed;
    }

    public static <A, R> Observer<A, R> fromFold(Fold<A, R> fold) {
        return new Observer<>(fold);
    }

    public static <A, R> Observer<A, R> fold(R seed, Function2<A, R, R> iteratee) {
        return new Observer<A, R>(Fold.fold(seed, iteratee));
    }

    public static <A> Observer<A, Object> forEach(Consumer<A> f) {
        return new Observer<>(Fold.forEach(f));
    }

    public static <A> Observer<A, List<A>> collect() {
        return new Observer<>(Fold.collect());
    }

    public static <A, B> Observer<A, List<B>> collectMap(Function<A, B> f) {
        return new Observer<>(Fold.collectMap(f));
    }

    public static <A> Observer<A, Long> count() {
        return new Observer<>(Fold.count());
    }

    public static <A> Observer<A, Optional<A>> last() {
        return new Observer<>(Fold.last());
    }

    public static <A> Observer<A, Optional<A>> first() {
        return new Observer<>(Fold.first());
    }
}

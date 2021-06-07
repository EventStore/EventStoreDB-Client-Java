package com.eventstore.dbclient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class Observer<A, R> {
    private final CompletableFuture<R> future = new CompletableFuture<>();
    private final Function2<A, R, R> iteratee;
    private R seed;
    private boolean completed = false;

    public Observer(R seed, Function2<A, R, R> iteratee) {
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

    public boolean isCompleted() {
        return completed;
    }

    public <B> Observer<B, R> contramap(Function<B, A> f) {
        return new Observer<>(seed, (b, acc) -> this.iteratee.apply(f.apply(b), acc));
    }

    public static <A, R> Observer<A, R> fold(R seed, Function2<A, R, R> iteratee) {
        return new Observer(seed, iteratee);
    }

    public static <A> Observer<A, Object> forEach(Consumer<A> f) {
        return new Observer<>(null, (a, x) -> {
            f.accept(a);
            return x;
        });
    }

    public static <A> Observer<A, List<A>> collect() {
        return new Observer<>(new ArrayList<>(), (a, acc) -> {
            acc.add(a);
            return acc;
        });
    }

    public static <A, B> Observer<A, List<B>> collectMap(Function<A, B> f) {
        return new Observer<>(new ArrayList<>(), (a, acc) -> {
            acc.add(f.apply(a));
            return acc;
        });
    }

    public static <A> Observer<A, Long> count() {
        return new Observer<>(Long.valueOf(0), (a, acc) -> acc + 1);
    }

    public static <A> Observer<A, A> last() {
        return new Observer<>(null, (a, x) -> a);
    }

    public static <A> Observer<A, A> first() {
        return new Observer<>(null, (a, x) -> {
           if (x == null) {
               return a;
           } else {
               return x;
           }
        });
    }
}

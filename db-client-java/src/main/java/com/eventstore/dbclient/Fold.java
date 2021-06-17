package com.eventstore.dbclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Fold<A, B> {
    Function2<A, Object, Object> step;
    Object begin;
    Function<Object, B> done;

    private Fold() {
    }

    // FOR INTERNAL USE ONLY.
    public Function2<A, Object, Object> getStep() {
        return step;
    }

    // FOR INTERNAL USE ONLY.
    public Object getBegin() {
        return begin;
    }

    // FOR INTERNAL USE ONLY.
    public Function<Object, B> getDone() {
        return done;
    }

    public static <A, B, X> Fold<A, B> create(Function2<A, X, X> step, X begin, Function<X, B> done) {
        Fold<A, B> result = new Fold<>();

        // Safe by construction.
        result.step = (a, obj) -> step.apply(a, (X) obj);
        result.begin = begin;
        result.done = (obj) -> done.apply((X) obj);

        return result;
    }

    public static <A, B> Fold<A, B> fold(B begin, Function2<A, B, B> step) {
        return Fold.create(step, begin, Function.identity());
    }

    public static <A> Fold<A, Optional<A>> first() {
        return Fold.<A, Optional<A>>fold(Optional.empty(), (a, prev) -> {
            if (prev.isPresent()) {
                return prev;
            } else {
                return Optional.of(a);
            }
        });
    }

    public static <A> Fold<A, Optional<A>> last() {
        return Fold.<A, Optional<A>, A>create((a, ignored) -> a, null, (a) -> {
            if (a == null) {
                return Optional.empty();
            } else {
                return Optional.of(a);
            }
        });
    }

    public static <A> Fold<A, Boolean> all(Predicate<A> p) {
        return Fold.<A, Boolean>fold(true, (a, prev) -> {
            if (prev) {
                return p.test(a);
            } else {
                return prev;
            }
        });
    }

    public static <A> Fold<A, Boolean> any(Predicate<A> p) {
        return Fold.<A, Boolean>fold(false, (a, prev) -> {
            if (!prev) {
                return p.test(a);
            } else {
                return prev;
            }
        });
    }

    public static <A> Fold<A, Long> count() {
        return Fold.<A, Long>fold(Long.valueOf(0), (ignored, prev) -> prev + 1);
    }

    public static <A> Fold<A, Optional<A>> find(Predicate<A> p) {
        return Fold.<A, Optional<A>>fold(Optional.empty(), (a, prev) -> {
            if (prev.isPresent() || !p.test(a)) {
                return prev;
            } else {
                return Optional.of(a);
            }
        });
    }

    public static <A> Fold<A, List<A>> collect() {
        return Fold.<A, List<A>>fold(new ArrayList<>(), (a, list) -> {
            list.add(a);
            return list;
        });
    }

    public static <A, B> Fold<A, List<B>> collectMap(Function<A, B> f) {
        return Fold.<A, List<B>>fold(new ArrayList<>(), (a, list) -> {
            list.add(f.apply(a));
            return list;
        });
    }

    public static <A> Fold<A, Object> forEach(Consumer<A> f) {
        return Fold.<A, Object>fold(null, (a, ignored) -> {
            f.accept(a);
            return ignored;
        });
    }

    public static <X, A> Fold<X, A> lift(A value) {
        return Fold.<X, A>fold(value, (ignored, v) -> v);
    }

    public <C, D> Fold<A, D> apply(Fold<A, C> other, Function2<B, C, D> f) {
        Fold<A, D> result = new Fold<>();

        Object[] values = new Object[] { this.begin, other.begin };

        // Safe by construction.
        result.step = (a, obj) -> {
            Object[] tmp = (Object[]) obj;

            tmp[0] = this.step.apply(a, tmp[0]);
            tmp[1] = other.step.apply(a, tmp[1]);

            return tmp;
        };
        result.begin = values;
        result.done = (obj) -> {
            Object[] tmp = (Object[]) obj;

            return f.apply(this.done.apply(tmp[0]), other.done.apply(tmp[1]));
        };

        return result;
    }

    public <C> Fold<A, C> map(Function<B, C> f) {
        Fold<A, C> result = new Fold<>();

        result.begin = this.begin;
        result.step = this.step;
        result.done = (obj) -> f.apply(this.done.apply(obj));

        return result;
    }

    public <C> Fold<C, B> contramap(Function<C, A> f) {
        Fold<C, B> result = new Fold<>();

        result.begin = this.begin;
        result.step = (c, obj) -> this.step.apply(f.apply(c), obj);
        result.done = this.done;

        return result;
    }

    public B fold(Iterable<A> values) {
        Object tmp = this.begin;

        for (A a : values) {
            tmp = this.step.apply(a, tmp);
        }

        return this.done.apply(tmp);
    }
}

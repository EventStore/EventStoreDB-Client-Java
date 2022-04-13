package com.eventstore.dbclient;

import java.util.stream.Stream;

public class StreamPosition<A> {
    public static <A> StreamPosition<A> start() {
        return new Start<>();
    }

    public static <A> StreamPosition<A> end() {
        return new End<>();
    }

    public static <A> StreamPosition<A> position(A position) {
        return new Position<>(position);
    }

    public boolean isStart() {
        return this instanceof Start;
    }

    public boolean isEnd() {
        return this instanceof End;
    }

    public final static class Start<A> extends StreamPosition<A> {}
    public final static class End<A> extends StreamPosition<A> {}
    public final static class Position<A> extends StreamPosition<A> {
        private final A position;

        public Position(A position) {
            this.position = position;
        }

        public A getPosition() {
            return position;
        }
    }
}

package com.eventstore.dbclient;

@FunctionalInterface
public interface Function2<A, B, R> {
   R apply(A a, B b);
}

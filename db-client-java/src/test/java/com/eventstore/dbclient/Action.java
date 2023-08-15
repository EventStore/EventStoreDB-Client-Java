package com.eventstore.dbclient;

@FunctionalInterface
public interface Action<A> {
    A run() throws Exception;
}

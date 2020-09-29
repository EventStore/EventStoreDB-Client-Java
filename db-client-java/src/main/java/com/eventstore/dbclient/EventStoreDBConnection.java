package com.eventstore.dbclient;

import io.grpc.ManagedChannel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface EventStoreDBConnection {
    <A> CompletableFuture<A> run(Function<ManagedChannel, CompletableFuture<A>> action);

    void shutdown() throws InterruptedException;
}

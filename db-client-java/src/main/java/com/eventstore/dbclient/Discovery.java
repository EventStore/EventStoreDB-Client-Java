package com.eventstore.dbclient;

import java.util.concurrent.CompletableFuture;

public interface Discovery {
    CompletableFuture<Void> run(ConnectionState state);
}
package com.eventstore.dbclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class EventStoreDBClientBase {
    final Logger logger = LoggerFactory.getLogger(EventStoreDBClientBase.class);
    final private GrpcClient client;

    protected EventStoreDBClientBase(EventStoreDBClientSettings settings) {
        Discovery discovery;

        if (settings.getHosts().length == 1) {
            discovery = new SingleNodeDiscovery(settings.getHosts()[0]);
        } else {
            discovery = new ClusterDiscovery(settings);
        }

        ConnectionService service = new ConnectionService(settings, discovery);
        this.client = service.getHandle();

        CompletableFuture.runAsync(service, createConnectionLoopExecutor());
    }
    private Executor createConnectionLoopExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "esdb-client-" + UUID.randomUUID());
            thread.setDaemon(true);
            return thread;
        });
    }


    /**
     * Closes a connection and cleans all its allocated resources.
     */
    public CompletableFuture<Void> shutdown() {
        return this.client.shutdown();
    }

    /**
     * Checks if this client instance has been shutdown.
     * After shutdown a client instance can no longer process new operations and
     * a new client instance has to be created.
     * @return {@code true} if client instance has been shutdown.
     */
    public boolean isShutdown() {
        return this.client.isShutdown();
    }

    GrpcClient getGrpcClient() {
        return client;
    }
}

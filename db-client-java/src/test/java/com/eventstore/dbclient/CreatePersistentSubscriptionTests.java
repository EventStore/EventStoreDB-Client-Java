package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.concurrent.CompletableFuture;

public class CreatePersistentSubscriptionTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testCreatePersistentSub() throws Throwable { {
        PersistentClient client = server.getPersistentClient();

        PersistentSubscriptionSettings settings = PersistentSubscriptionSettings.builder().build();
        CompletableFuture result = client.create(settings, "aStream", "aGroup");

        result.get();
    }}
}

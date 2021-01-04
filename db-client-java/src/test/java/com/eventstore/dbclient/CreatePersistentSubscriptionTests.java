package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

public class CreatePersistentSubscriptionTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Test
    public void testCreatePersistentSub() throws Throwable { {
        PersistentSubscriptions persistent = server.getPersistentSubscriptionsAPI();

        persistent.create("aStream", "aGroup")
                .get();
    }}
}

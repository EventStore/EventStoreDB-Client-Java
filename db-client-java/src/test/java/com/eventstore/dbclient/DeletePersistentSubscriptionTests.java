package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

public class DeletePersistentSubscriptionTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Test
    public void testDeletePersistentSub() throws Throwable { {
        PersistentSubscriptions persistent = server.getPersistentSubscriptionsAPI();

        persistent.create("aStream", "aGroupUpd")
                .get();

        persistent.delete("aStream", "aGroupUpd")
                .get();
    }}
}

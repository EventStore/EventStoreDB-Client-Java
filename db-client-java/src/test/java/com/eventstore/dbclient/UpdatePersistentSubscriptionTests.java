package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

public class UpdatePersistentSubscriptionTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Test
    public void testUpdatePersistentSub() throws Throwable { {
        PersistentSubscriptions persistent = server.getPersistentSubscriptionsAPI();

        persistent.create("aStream", "aGroupUpd")
                .execute()
                .get();

        PersistentSubscriptionSettings updatedSettings = PersistentSubscriptionSettings.builder()
                .checkpointAfterInMs(5_000)
                .build();

        persistent.update("aStream", "aGroupUpd")
            .settings(updatedSettings)
            .execute()
            .get();
    }}
}

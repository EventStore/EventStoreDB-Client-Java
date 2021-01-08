package com.eventstore.dbclient;

import org.junit.Test;

public class UpdatePersistentSubscriptionTests extends PersistenSubscriptionTestsBase {
    @Test
    public void testUpdatePersistentSub() throws Throwable {

        client.create("aStream", "aGroupUpd")
                .get();

        PersistentSubscriptionSettings updatedSettings = PersistentSubscriptionSettings.builder()
                .checkpointAfterInMs(5_000)
                .build();

        UpdatePersistentSubscriptionOptions options = UpdatePersistentSubscriptionOptions.get()
                .settings(updatedSettings);

        client.update("aStream", "aGroupUpd", options)
            .get();
    }
}

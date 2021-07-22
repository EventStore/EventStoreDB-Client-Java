package com.eventstore.dbclient;

import org.junit.Test;

public class UpdatePersistentSubscriptionTests extends PersistenSubscriptionTestsBase {
    @Test
    public void testUpdatePersistentSub() throws Throwable {

        client.create("aStream", "aGroupUpd")
                .get();

        PersistentSubscriptionSettings updatedSettings = PersistentSubscriptionSettings.builder()
                .checkpointAfterInMs(5_000)
                .revision(2)
                .build();

        UpdatePersistentSubscriptionOptions options = UpdatePersistentSubscriptionOptions.get()
                .settings(updatedSettings);

        client.update("aStream", "aGroupUpd", options)
                .get();
    }

    @Test
    public void testUpdatePersistentSubToAll() throws Throwable {

        client.createToAll("aGroupUpd")
                .get();

        PersistentSubscriptionToAllSettings updatedSettings = PersistentSubscriptionToAllSettings.builder()
                .checkpointAfterInMs(5_000)
                .position(4,3)
                .build();

        UpdatePersistentSubscriptionToAllOptions options = UpdatePersistentSubscriptionToAllOptions.get()
                .settings(updatedSettings);

        client.updateToAll("aGroupUpd", options)
                .get();
    }
}

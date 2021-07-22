package com.eventstore.dbclient;

import org.junit.Test;

public class CreatePersistentSubscriptionTests extends PersistenSubscriptionTestsBase {
    @Test
    public void testCreatePersistentSub() throws Throwable {
        client.create("aStream", "aGroup")
                .get();

        client.create("aStream", "bGroup", PersistentSubscriptionSettings.builder()
                .revision(1).build())
                .get();
    }

    @Test
    public void testCreatePersistentSubToAll() throws Throwable {
        client.createToAll("aGroup")
                .get();

        client.createToAll("bGroup", PersistentSubscriptionToAllSettings.builder()
                .position(2, 1).build())
                .get();
    }
}

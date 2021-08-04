package com.eventstore.dbclient;

import org.junit.Test;

public class DeletePersistentSubscriptionTests extends PersistenSubscriptionTestsBase {
    @Test
    public void testDeletePersistentSub() throws Throwable {
        client.create("aStream", "aGroupUpd")
                .get();

        client.delete("aStream", "aGroupUpd")
                .get();
    }

    @Test
    public void testDeletePersistentSubToAll() throws Throwable {
        client.createToAll("aGroupUpd")
                .get();

        client.deleteToAll("aGroupUpd")
                .get();
    }
}

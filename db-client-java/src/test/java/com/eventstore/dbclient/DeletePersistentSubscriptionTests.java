package com.eventstore.dbclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

public class DeletePersistentSubscriptionTests extends PersistenSubscriptionTestsBase {
    @Test
    public void testDeletePersistentSub() throws Throwable {
        client.create("aStream", "aGroupUpd")
                .get();

        client.delete("aStream", "aGroupUpd")
                .get();
    }
}

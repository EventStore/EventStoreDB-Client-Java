package com.eventstore.dbclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

public class CreatePersistentSubscriptionTests extends PersistenSubscriptionTestsBase {
    @Test
    public void testCreatePersistentSub() throws Throwable {
        client.create("aStream", "aGroup")
                .get();
    }

    @Test
    public void testCreatePersistentSubToAll() throws Throwable {
        client.createToAll("aGroup")
                .get();
    }
}

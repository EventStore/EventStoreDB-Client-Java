package com.eventstore.dbclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.concurrent.ExecutionException;

class PersistenSubscriptionTestsBase {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);
    protected EventStoreDBPersistentSubscriptionsClient client;

    @Before
    public void before() throws Throwable {
        client = server.getPersistentSubscriptionsClient();
    }

    @After
    public void after() {
        if(client == null)
            return;

        try {
            client.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}

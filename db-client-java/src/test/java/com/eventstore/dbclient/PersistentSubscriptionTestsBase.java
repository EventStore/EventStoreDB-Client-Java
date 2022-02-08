package com.eventstore.dbclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.concurrent.ExecutionException;

class PersistentSubscriptionTestsBase {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(true);
    protected EventStoreDBPersistentSubscriptionsClient client;
    protected EventStoreDBClient streamClient;

    @Before
    public void before() throws Throwable {
        client = server.getPersistentSubscriptionsClient();
        streamClient = server.getClient();
    }

    @After
    public void after() {
        if(client == null && streamClient == null)
            return;

        try {
            if (client != null)
                client.shutdown();

            if (streamClient != null)
                streamClient.shutdown();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}

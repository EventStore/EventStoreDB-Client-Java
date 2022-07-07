package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.concurrent.ExecutionException;

public class DeletePersistentSubscriptionToStreamTests extends ESDBTests {
    @Test
    public void testDeletePersistentSub() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String streamName = generateName();
        String groupName = generateName();

        client.createToStream(streamName, groupName)
                .get();

        client.deleteToStream(streamName, groupName)
                .get();
    }

    @Test
    public void testDeletePersistentSubToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();
        String groupName = generateName();

        try {
            client.createToAll(groupName)
                    .get();

            client.deleteToAll(groupName)
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeatureException && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }
        }
    }
}

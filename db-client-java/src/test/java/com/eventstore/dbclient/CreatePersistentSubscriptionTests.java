package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.concurrent.ExecutionException;

public class CreatePersistentSubscriptionTests extends ESDBTests {
    @Test
    public void testCreatePersistentSub() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();

        client.createToStream(generateName(), generateName())
                .get();

        client.createToStream(generateName(), generateName(), CreatePersistentSubscriptionToStreamOptions.get().startFrom(2))
                .get();
    }

    @Test
    public void testCreatePersistentSubToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();

        try {
            client.createToAll(generateName())
                    .get();


            client.createToAll(generateName(), CreatePersistentSubscriptionToAllOptions.get().startFrom(1, 2))
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeatureException && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }
        }
    }
}

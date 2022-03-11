package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.concurrent.ExecutionException;

public class CreatePersistentSubscriptionTests extends ESDBTests {
    @Test
    public void testCreatePersistentSub() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();

        client.create(generateName(), generateName())
                .get();

        client.create(generateName(), generateName(), PersistentSubscriptionSettings.builder().startFrom(1).build())
                .get();
    }

    @Test
    public void testCreatePersistentSubToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getEmptyServer().getPersistentSubscriptionsClient();

        try {
            client.createToAll(generateName())
                    .get();

            client.createToAll(generateName(), PersistentSubscriptionToAllSettings.builder()
                            .startFrom(2, 1).build())
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeature && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }
        }
    }
}

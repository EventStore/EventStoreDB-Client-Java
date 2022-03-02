package com.eventstore.dbclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.concurrent.ExecutionException;

public class UpdatePersistentSubscriptionTests extends ESDBTests {
    private EventStoreDBPersistentSubscriptionsClient client;

    @BeforeEach
    public void init() {
        client = getEmptyServer().getPersistentSubscriptionsClient();
    }

    @Test
    public void testUpdatePersistentSub() throws Throwable {
        String streamName = generateName();
        String groupName = generateName();

        client.create(streamName, groupName)
                .get();

        PersistentSubscriptionSettings updatedSettings = PersistentSubscriptionSettings.builder()
                .checkpointAfterInMs(5_000)
                .startFrom(2)
                .build();

        UpdatePersistentSubscriptionOptions options = UpdatePersistentSubscriptionOptions.get()
                .settings(updatedSettings);

        client.update(streamName, groupName, options)
                .get();
    }

    @Test
    public void testUpdatePersistentSubToAll() throws Throwable {
        String groupName = generateName();

        try {
            client.createToAll(groupName)
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeature && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }

            return;
        }

        PersistentSubscriptionToAllSettings updatedSettings = PersistentSubscriptionToAllSettings.builder()
                .checkpointAfterInMs(5_000)
                .startFrom(4,3)
                .build();

        UpdatePersistentSubscriptionToAllOptions options = UpdatePersistentSubscriptionToAllOptions.get()
                .settings(updatedSettings);

        client.updateToAll(groupName, options)
                .get();
    }
}

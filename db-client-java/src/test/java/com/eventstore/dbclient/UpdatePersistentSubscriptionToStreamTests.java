package com.eventstore.dbclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.concurrent.ExecutionException;

public class UpdatePersistentSubscriptionToStreamTests extends ESDBTests {
    private EventStoreDBPersistentSubscriptionsClient client;

    @BeforeEach
    public void init() {
        client = getEmptyServer().getPersistentSubscriptionsClient();
    }

    @Test
    public void testUpdatePersistentSub() throws Throwable {
        String streamName = generateName();
        String groupName = generateName();

        client.createToStream(streamName, groupName)
                .get();

        UpdatePersistentSubscriptionToStreamOptions updated = UpdatePersistentSubscriptionToStreamOptions.get()
                .checkpointAfterInMs(5_000)
                .startFrom(2);

        client.updateToStream(streamName, groupName, updated)
                .get();
    }

    @Test
    public void testUpdatePersistentSubToAll() throws Throwable {
        String groupName = generateName();

        try {
            client.createToAll(groupName)
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnsupportedFeatureException && !EventStoreDB.isTestedAgainstVersion20()) {
                throw e;
            }

            return;
        }

        UpdatePersistentSubscriptionToAllOptions updatedSettings = UpdatePersistentSubscriptionToAllOptions.get()
                .checkpointAfterInMs(5_000)
                .startFrom(3,4);

        client.updateToAll(groupName, updatedSettings)
                .get();
    }
}

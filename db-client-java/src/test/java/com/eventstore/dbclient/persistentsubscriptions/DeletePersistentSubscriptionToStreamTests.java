package com.eventstore.dbclient.persistentsubscriptions;

import com.eventstore.dbclient.ConnectionAware;
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient;
import com.eventstore.dbclient.UnsupportedFeatureException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public interface DeletePersistentSubscriptionToStreamTests extends ConnectionAware {
    @Test
    default void testDeletePersistentSub() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = EventStoreDBPersistentSubscriptionsClient.from(getDatabase().defaultClient());
        String streamName = generateName();
        String groupName = generateName();

        client.createToStream(streamName, groupName)
                .get();

        client.deleteToStream(streamName, groupName)
                .get();
    }

    @Test
    default void testDeletePersistentSubToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = EventStoreDBPersistentSubscriptionsClient.from(getDatabase().defaultClient());
        String groupName = generateName();

        client.createToAll(groupName)
                .get();

        client.deleteToAll(groupName)
                .get();
    }
}

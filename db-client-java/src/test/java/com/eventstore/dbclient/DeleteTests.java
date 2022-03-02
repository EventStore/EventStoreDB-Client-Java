package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

public class DeleteTests extends ESDBTests {
    @Test
    public void testCanDeleteStream() throws Throwable {
        EventStoreDBClient client = getEmptyServer().getClient();
        String streamName = generateName();

        client.appendToStream(streamName, generateEvents(1, "foobar").iterator()).get();

        client.deleteStream(streamName).get();
    }
}

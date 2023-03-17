package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ClientLifecycleTests extends ESDBTests {
    @Test
    public void testProvidesRunningStatus() {
        EventStoreDBClientSettings settings = getEmptyServer().getSettings();

        EventStoreDBClient client = EventStoreDBClient.create(settings);

        assertFalse(client.isShutdown());
    }

    @Test
    public void testProvidesShutdownStatusAfterManualShutdown() throws Throwable {
        EventStoreDBClientSettings settings = getEmptyServer().getSettings();
        EventStoreDBClient client = EventStoreDBClient.create(settings);

        client.shutdown().get();

        assertTrue(client.isShutdown());
    }

    @Test
    public void testProvidesShutdownStatusAfterAutomaticShutdown() throws Throwable {
        EventStoreDBClientSettings settings = EventStoreDBClientSettings.builder()
                .addHost("unknown.host.name", 2113)
                .buildConnectionSettings();
        EventStoreDBClient client = EventStoreDBClient.create(settings);

        try {
            client.readAll().get();
            fail();
        } catch (ExecutionException ex) {
            assertInstanceOf(ConnectionShutdownException.class, ex.getCause());
        }
        assertTrue(client.isShutdown());
    }

}

package com.eventstore.dbclient;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
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

    @Test
    public void testDeleteStreamWhenAlreadyDeleted() throws Throwable {
        EventStoreDBClient client = getEmptyServer().getClient();
        String streamName = generateName();

        client.appendToStream(streamName, generateEvents(1, "foobar").iterator()).get();
        client.tombstoneStream(streamName, DeleteStreamOptions.get()).get();
        Assertions.assertThrows(StreamDeletedException.class, () -> {
            try {
                client.tombstoneStream(streamName, DeleteStreamOptions.get()).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testDeleteStreamWhenDoesntExist() throws Throwable {
        EventStoreDBClient client = getEmptyServer().getClient();
        Optional<ServerVersion> serverVersion = client.getGrpcClient().getServerVersion().get();

        // There are some versions of the server that will not send wrong expected version in this case.
        // Those version just send gRPC status Unknown in this case.
        if (!serverVersion.isPresent() || serverVersion.get().getMajor() <= 20) {
            return;
        }

        String streamName = generateName();
        DeleteStreamOptions options = DeleteStreamOptions.get()
            .expectedRevision(ExpectedRevision.streamExists());

        Assertions.assertThrows(WrongExpectedVersionException.class, () -> {
            try {
                client.tombstoneStream(streamName, options).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }
}

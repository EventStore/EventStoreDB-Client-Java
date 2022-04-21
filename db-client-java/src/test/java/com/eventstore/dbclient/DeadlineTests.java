package com.eventstore.dbclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class DeadlineTests extends ESDBTests {
    @Test
    public void testDefaultDeadline() throws Throwable {
        EventStoreDBClient client = getEmptyServer().getClientWithSettings("tls=false&defaultdeadline=1");
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();
        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.appendToStream("toto", data).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(status.getStatus().getCode(), Status.Code.DEADLINE_EXCEEDED);

        client.shutdown();
    }

    @Test
    public void testOptionLevelDeadline() throws Throwable {
        EventStoreDBClient client = getEmptyServer().getClientWithSettings("tls=false");
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();
        AppendToStreamOptions options = AppendToStreamOptions.get().deadline(1);
        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.appendToStream("toto", options, data).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(status.getStatus().getCode(), Status.Code.DEADLINE_EXCEEDED);

        client.shutdown();
    }
}

package com.eventstore.dbclient.misc;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventDataBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class EventDataTests {
    @Test
    public void testBinaryConstructorWithId() throws Throwable {
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();

        Assertions.assertEquals(id, data.getEventId());
    }

    @Test
    public void testJsonConstructorWithId() throws Throwable {
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.json(id, "type", new byte[]{}).build();

        Assertions.assertEquals(id, data.getEventId());
    }
}

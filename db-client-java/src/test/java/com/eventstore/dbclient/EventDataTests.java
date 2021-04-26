package com.eventstore.dbclient;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class EventDataTests {
    @Test
    public void testBinaryConstructorWithId() throws Throwable {
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();

        Assert.assertEquals(id, data.getEventId());
    }

    @Test
    public void testJsonConstructorWithId() throws Throwable {
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.json(id, "type", new byte[]{}).build();

        Assert.assertEquals(id, data.getEventId());
    }
}

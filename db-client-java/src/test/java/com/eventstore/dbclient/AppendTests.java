package com.eventstore.dbclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

class Foo {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Foo foo1 = (Foo) o;
        return foo == foo1.foo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(foo);
    }

    private boolean foo;

    public boolean isFoo() {
        return foo;
    }

    public void setFoo(boolean foo) {
        this.foo = foo;
    }
}

public class AppendTests extends ESDBTests {
    @Test
    public void testAppendSingleEventNoStream() throws Throwable {
        EventStoreDBClient client = getEmptyServer().getClient();

        final String streamName = generateName();
        final String eventType = "TestEvent";
        final String eventId = "38fffbc2-339e-11ea-8c7b-784f43837872";
        final byte[] eventMetaData = new byte[]{0xd, 0xe, 0xa, 0xd};

        EventData event = EventData.builderAsJson(eventType, new Foo())
                .metadataAsBytes(eventMetaData)
                .eventId(UUID.fromString(eventId))
                .build();

        AppendToStreamOptions appendOptions = AppendToStreamOptions.get()
                .expectedRevision(ExpectedRevision.noStream());

        WriteResult appendResult = client.appendToStream(streamName, appendOptions, event)
                .get();

        Assertions.assertEquals(0, appendResult.getNextExpectedRevision());

        ReadStreamOptions readStreamOptions = ReadStreamOptions.get()
                .fromEnd()
                .backwards()
                .maxCount(1);

        // Ensure appended event is readable
        ReadResult result = client.readStream(streamName, readStreamOptions)
                .get();

        Assertions.assertEquals(1, result.getEvents().size());
        RecordedEvent first = result.getEvents().get(0).getEvent();

        Assertions.assertEquals(streamName, first.getStreamId());
        Assertions.assertEquals(eventType, first.getEventType());
        Assertions.assertEquals(eventId, first.getEventId().toString());
        Assertions.assertArrayEquals(eventMetaData, first.getUserMetadata());
        Assertions.assertEquals(new Foo(), first.getEventDataAs(Foo.class));
    }
}

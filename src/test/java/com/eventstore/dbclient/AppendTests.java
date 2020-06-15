package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;
import testcontainers.module.EventStoreStreamsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AppendTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(true);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testAppendSingleEventNoStream() throws Throwable {
        final String streamName = "testIntegrationAppendSingleEventNoStream";
        final String eventType = "TestEvent";
        final String eventId = "38fffbc2-339e-11ea-8c7b-784f43837872";
        final byte[] eventMetaData = new byte[]{0xd, 0xe, 0xa, 0xd};
        final byte[] eventData = new byte[]{0xb, 0xe, 0xe, 0xf};

        // Append event
        List<ProposedEvent> events = new ArrayList<>();
        events.add(new ProposedEvent(UUID.fromString(eventId), eventType, "application/octet-stream", eventData, eventMetaData));

        CompletableFuture<WriteResult> appendFuture = client.instance.appendToStream(streamName, SpecialStreamRevision.NO_STREAM, events);
        WriteResult appendResult = appendFuture.get();

        assertEquals(new StreamRevision(0), appendResult.getNextExpectedRevision());

        // Ensure appended event is readable
        CompletableFuture<ReadResult> readFuture = client.instance.readStream(Direction.Backward, streamName, StreamRevision.END, 1, false);
        ReadResult readResult = readFuture.get();
        List<ResolvedEvent> readEvents = readResult.getEvents();
        assertEquals(1, readEvents.size());
        RecordedEvent first = readEvents.get(0).getEvent();

        assertEquals(streamName, first.getStreamId());
        assertEquals(eventType, first.getEventType());
        assertEquals(eventId, first.getEventId().toString());
        assertArrayEquals(eventMetaData, first.getUserMetadata());
        assertArrayEquals(eventData, first.getEventData());
    }
}

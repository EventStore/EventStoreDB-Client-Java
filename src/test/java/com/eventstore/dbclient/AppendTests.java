package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;
import testcontainers.module.EventStoreStreamsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

        List<ProposedEvent> events = new ArrayList<>();
        events.add(new ProposedEvent(UUID.fromString(eventId), eventType, "application/octet-stream", eventData, eventMetaData));

        CompletableFuture<WriteResult> future = client.instance.appendToStream(streamName, SpecialStreamRevision.NO_STREAM, events);
        WriteResult result = future.get();

        assertEquals(new StreamRevision(0), result.getNextExpectedRevision());
    }
}

package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReadStreamTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testReadStreamForward10EventsFromPositionStart() throws Throwable {
        CompletableFuture<ReadResult> future = client.instance.readStream(
                Direction.Forward,
                "dataset20M-1800",
                StreamRevision.START,
                10,
                false);
        ReadResult result = future.get();
        verifyAgainstTestData(result.getEvents(), "dataset20M-1800-e0-e10");
    }

    @Test
    public void testReadStreamBackward10EventsFromPositionEnd() throws Throwable {
        CompletableFuture<ReadResult> future = client.instance.readStream(
                Direction.Backward,
                "dataset20M-1800",
                StreamRevision.END,
                10,
                false);
        ReadResult result = future.get();
        verifyAgainstTestData(result.getEvents(), "dataset20M-1800-e1999-e1990");
    }

    private void verifyAgainstTestData(List<ResolvedEvent> actualEvents, String filenameStem) {
        ResolvedEvent[] actualEventsArray = actualEvents.toArray(new ResolvedEvent[0]);

        TestResolvedEvent[] expectedEvents = TestDataLoader.loadSerializedTestData(filenameStem);
        for (int i = 0; i < expectedEvents.length; i++) {
            TestResolvedEvent expected = expectedEvents[i];
            ResolvedEvent actual = actualEventsArray[i];

            expected.assertEquals(actual);
        }
    }
}

package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.List;

public class ReadStreamTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Test
    public void testReadStreamForward10EventsFromPositionStart() throws Throwable {
        Streams streams = server.getStreamsAPI();

        ReadStreamOptions options = ReadStreamOptions.get()
                .forward()
                .fromStart()
                .notResolveLinkTos();

        ReadResult result = streams.readStream("dataset20M-1800", 10, options)
                .get();

        verifyAgainstTestData(result.getEvents(), "dataset20M-1800-e0-e10");
    }

    @Test
    public void testReadStreamBackward10EventsFromPositionEnd() throws Throwable {
        Streams streams = server.getStreamsAPI();

        ReadStreamOptions options = ReadStreamOptions.get()
                .backward()
                .fromEnd()
                .notResolveLinkTos();

        ReadResult result = streams.readStream("dataset20M-1800", 10, options)
                .get();

        verifyAgainstTestData(result.getEvents(), "dataset20M-1800-e1999-e1990");
    }

    private void verifyAgainstTestData(List<ResolvedEvent> actualEvents, String filenameStem) {
        ResolvedEvent[] actualEventsArray = actualEvents.toArray(new ResolvedEvent[0]);

        TestResolvedEvent[] expectedEvents = TestDataLoader.loadSerializedResolvedEvents(filenameStem);
        for (int i = 0; i < expectedEvents.length; i++) {
            TestResolvedEvent expected = expectedEvents[i];
            ResolvedEvent actual = actualEventsArray[i];

            expected.assertEquals(actual);
        }
    }
}

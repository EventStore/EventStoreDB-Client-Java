package com.eventstore.dbclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.List;

public class ReadStreamTests extends ESDBTests {
    @Test
    public void testReadStreamForward10EventsFromPositionStart() throws Throwable {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .maxCount(10)
                .notResolveLinkTos();

        List<ResolvedEvent> result = client.readStream("dataset20M-1800", options)
                .get();

        verifyAgainstTestData(result, "dataset20M-1800-e0-e10");
    }

    @Test
    public void testReadStreamBackward10EventsFromPositionEnd() throws Throwable {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .maxCount(10)
                .notResolveLinkTos();

        List<ResolvedEvent> result = client.readStream("dataset20M-1800", options)
                .get();

        verifyAgainstTestData(result, "dataset20M-1800-e1999-e1990");
    }

    @Test
    public void testReadStreamOptionsAreNotIgnoredInOverloadedMethod() throws Throwable {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .notResolveLinkTos();

        List<ResolvedEvent> result1 = client.readStream("dataset20M-1800", options)
                .get();

        List<ResolvedEvent> result2 = client.readStream("dataset20M-1800", options)
                .get();

        RecordedEvent firstEvent1 = result1.get(0).getOriginalEvent();
        RecordedEvent firstEvent2 = result2.get(0).getOriginalEvent();

        Assertions.assertEquals(result1.size(), result2.size());
        Assertions.assertEquals(firstEvent1.getEventId(), firstEvent2.getEventId());
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

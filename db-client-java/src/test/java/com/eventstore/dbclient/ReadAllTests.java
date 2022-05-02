package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReadAllTests extends ESDBTests {
    @Test
    public void testReadAllEventsForwardFromZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos()
                .maxCount(10);

        ReadResult result = client.readAll(options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-e0-e10");
    }

    @Test
    public void testReadAllEventsForwardFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromPosition(new Position(1788, 1788))
                .notResolveLinkTos()
                .maxCount(10);

        ReadResult result = client.readAll(options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-c1788-p1788");
    }

    @Test
    public void testReadAllEventsBackwardsFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromPosition(new Position(3386, 3386))
                .notResolveLinkTos()
                .maxCount(10);

        ReadResult result = client.readAll(options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-back-c3386-p3386");
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

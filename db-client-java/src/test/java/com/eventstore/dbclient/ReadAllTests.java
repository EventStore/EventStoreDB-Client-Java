package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReadAllTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Test
    public void testReadAllEventsForwardFromZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = server.getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos();

        ReadResult result = client.readAll(10, options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-e0-e10");
    }

    @Test
    public void testReadAllEventsForwardFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = server.getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromPosition(new Position(1788, 1788))
                .notResolveLinkTos();

        ReadResult result = client.readAll(10, options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-c1788-p1788");
    }

    @Test
    public void testReadAllEventsBackwardsFromZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = server.getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromEnd()
                .notResolveLinkTos();

        ReadResult result = client.readAll(10, options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-back-e0-e10");
    }

    @Test
    public void testReadAllEventsBackwardsFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = server.getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromPosition(new Position(3386, 3386))
                .notResolveLinkTos();

        ReadResult result = client.readAll(10, options)
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

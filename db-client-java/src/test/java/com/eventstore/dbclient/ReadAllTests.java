package com.eventstore.dbclient;

import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ReadAllTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testReadAllEventsForwardFromZeroPosition() throws ExecutionException, InterruptedException {
        CompletableFuture<ReadResult> future = client.instance.readAll(Direction.Forward, Position.START, 10, false);
        ReadResult result = future.get();
        verifyAgainstTestData(result.getEvents(), "all-e0-e10");
    }

    @Test
    public void testReadAllEventsForwardFromNonZeroPosition() throws ExecutionException, InterruptedException {
        CompletableFuture<ReadResult> future = client.instance.readAll(Direction.Forward,
                new Position(1788, 1788),
                10, false);
        ReadResult result = future.get();
        verifyAgainstTestData(result.getEvents(), "all-c1788-p1788");
    }

    @Test
    public void testReadAllEventsBackwardsFromZeroPosition() throws ExecutionException, InterruptedException {
        CompletableFuture<ReadResult> future = client.instance.readAll(
                Direction.Backward, Position.END, 10, false);
        ReadResult result = future.get();
        verifyAgainstTestData(result.getEvents(), "all-back-e0-e10");
    }

    @Test
    public void testReadAllEventsBackwardsFromNonZeroPosition() throws ExecutionException, InterruptedException {
        CompletableFuture<ReadResult> future = client.instance.readAll(
                Direction.Backward, new Position(3386, 3386),
                10, false);
        ReadResult result = future.get();
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

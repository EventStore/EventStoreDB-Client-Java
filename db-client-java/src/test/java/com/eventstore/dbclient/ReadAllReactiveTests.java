package com.eventstore.dbclient;

import io.reactivex.rxjava3.core.Flowable;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

public class ReadAllReactiveTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Test
    public void testReadAllEventsReactiveForwardFromZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = server.getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos();

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readAllReactive(10, options))
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(events, "all-e0-e10");
    }

    @Test
    public void testReadAllEventsReactiveForwardFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = server.getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromPosition(new Position(1788, 1788))
                .notResolveLinkTos();

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readAllReactive(10, options))
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(events, "all-c1788-p1788");
    }

    @Test
    public void testReadAllEventsReactiveBackwardsFromZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = server.getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromEnd()
                .notResolveLinkTos();

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readAllReactive(10, options))
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(events, "all-back-e0-e10");
    }

    @Test
    public void testReadAllEventsReactiveBackwardsFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = server.getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromPosition(new Position(3386, 3386))
                .notResolveLinkTos();

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readAllReactive(10, options))
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(events, "all-back-c3386-p3386");
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

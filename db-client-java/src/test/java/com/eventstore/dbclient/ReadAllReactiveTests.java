package com.eventstore.dbclient;

import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

public class ReadAllReactiveTests extends ESDBTests {
    @Test
    public void testReadAllEventsReactiveForwardFromZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos()
                .maxCount(10);

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readAllReactive(options))
                .filter(ReadMessage::hasEvent)
                .map(ReadMessage::getEvent)
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(events, "all-e0-e10");
    }

    @Test
    public void testReadAllEventsReactiveForwardFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromPosition(new Position(1788, 1788))
                .notResolveLinkTos()
                .maxCount(10);

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readAllReactive(options))
                .filter(ReadMessage::hasEvent)
                .map(ReadMessage::getEvent)
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(events, "all-c1788-p1788");
    }

    @Test
    public void testReadAllEventsReactiveBackwardsFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromPosition(new Position(3386, 3386))
                .notResolveLinkTos()
                .maxCount(10);

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readAllReactive(options))
                .filter(ReadMessage::hasEvent)
                .map(ReadMessage::getEvent)
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

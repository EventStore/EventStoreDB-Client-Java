package com.eventstore.dbclient;

import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;
import testcontainers.module.EventStoreDB;

import java.util.List;
import java.util.Optional;

public class ReadStreamTests extends ESDBTests {
    @Test
    public void testReadStreamForward10EventsFromPositionStart() throws Throwable {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .maxCount(10)
                .notResolveLinkTos();

        ReadResult result = client.readStream("dataset20M-1800", options)
                .get();

        verifyAgainstTestData(
                result.getEvents(),
                "dataset20M-1800-e0-e10",
                client.getGrpcClient().getServerVersion().get());
    }

    @Test
    public void testReadStreamBackward10EventsFromPositionEnd() throws Throwable {
        EventStoreDBClient client = getPopulatedServer().getClient();
        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .maxCount(10)
                .notResolveLinkTos();

        ReadResult result = client.readStream("dataset20M-1800", options)
                .get();

        verifyAgainstTestData(
                result.getEvents(),
                "dataset20M-1800-e1999-e1990",
                client.getGrpcClient().getServerVersion().get());
    }

    @Test
    public void testReadStreamOptionsAreNotIgnoredInOverloadedMethod() throws Throwable {
        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .notResolveLinkTos();

        ReadResult result1 = client.readStream("dataset20M-1800", options)
                .get();

        ReadResult result2 = client.readStream("dataset20M-1800", options)
                .get();

        RecordedEvent firstEvent1 = result1.getEvents().get(0).getOriginalEvent();
        RecordedEvent firstEvent2 = result2.getEvents().get(0).getOriginalEvent();

        Assertions.assertEquals(result1.getEvents().size(), result2.getEvents().size());
        Assertions.assertEquals(firstEvent1.getEventId(), firstEvent2.getEventId());
    }

    @Test
    public void testStreamPositionWhenReading() throws Throwable {
        if (EventStoreDB.isTestedAgains20_10()) {
            return;
        }

        EventStoreDBClient client = getPopulatedServer().getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos();

        long last = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", options))
                .filter(ReadMessage::hasLastStreamPosition)
                .map(ReadMessage::getLastStreamPosition)
                .blockingFirst();

        Assertions.assertEquals(1_999, last);
    }


    private void verifyAgainstTestData(List<ResolvedEvent> actualEvents, String filenameStem, Optional<ServerVersion> serverVersion) {
        ResolvedEvent[] actualEventsArray = actualEvents.toArray(new ResolvedEvent[0]);

        TestResolvedEvent[] expectedEvents = TestDataLoader.loadSerializedResolvedEvents(filenameStem);
        for (int i = 0; i < expectedEvents.length; i++) {
            TestResolvedEvent expected = expectedEvents[i];
            ResolvedEvent actual = actualEventsArray[i];

            if (!serverVersion.isPresent() || serverVersion.get().isLessThan(22, 6, 0)) {
                TestPosition notSet = new TestPosition();
                notSet.setPrepare(-1);
                notSet.setCommit(-1);
                expected.getEvent().setPosition(notSet);
            }

            expected.assertEquals(actual);
        }
    }
}

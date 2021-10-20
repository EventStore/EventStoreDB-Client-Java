package com.eventstore.dbclient;

import io.reactivex.rxjava3.core.Flowable;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ReadStreamReactiveTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Test
    public void testReadStreamReactiveForward10EventsFromPositionStart() throws Throwable {
        EventStoreDBClient client = server.getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos();

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", 10, options))
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(events, "dataset20M-1800-e0-e10");
    }

    @Test
    public void testReadStreamReactiveBackward10EventsFromPositionEnd() throws Throwable {
        EventStoreDBClient client = server.getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .notResolveLinkTos();

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", 10, options))
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(events, "dataset20M-1800-e1999-e1990");
    }

    @Test
    public void testReadStreamOptionsAreNotIgnoredInOverloadedMethod() throws Throwable {
        EventStoreDBClient client = server.getClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .notResolveLinkTos();

        List<ResolvedEvent> events1 = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", Long.MAX_VALUE, options))
                .collect(toList())
                .blockingGet();

        List<ResolvedEvent> events2 = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", options))
                .collect(toList())
                .blockingGet();

        RecordedEvent firstEvent1 = events1.get(0).getOriginalEvent();
        RecordedEvent firstEvent2 = events2.get(0).getOriginalEvent();

        Assert.assertEquals(events1.size(), events2.size());
        Assert.assertEquals(firstEvent1.getEventId(), firstEvent2.getEventId());
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

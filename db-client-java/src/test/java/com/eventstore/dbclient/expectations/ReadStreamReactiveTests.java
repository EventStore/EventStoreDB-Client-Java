package com.eventstore.dbclient.expectations;

import com.eventstore.dbclient.*;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;

public interface ReadStreamReactiveTests extends Expectations {
    @Test
    default void testReadStreamReactiveForward10EventsFromPositionStart() throws Throwable {
        Assumptions.assumeFalse(getDatabase().isTargetingBelowOrEqual21_10());

        EventStoreDBClient client = getDefaultClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos()
                .maxCount(10);

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", options))
                .filter(ReadMessage::hasEvent)
                .map(ReadMessage::getEvent)
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(
                events,
                "dataset20M-1800-e0-e10");
    }

    @Test
    default void testReadStreamReactiveBackward10EventsFromPositionEnd() throws Throwable {
        Assumptions.assumeFalse(getDatabase().isTargetingBelowOrEqual21_10());

        EventStoreDBClient client = getDefaultClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .notResolveLinkTos()
                .maxCount(10);

        List<ResolvedEvent> events = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", options))
                .filter(ReadMessage::hasEvent)
                .map(ReadMessage::getEvent)
                .collect(toList())
                .blockingGet();

        verifyAgainstTestData(
                events,
                "dataset20M-1800-e1999-e1990");
    }

    @Test
    default void testReadStreamReactiveOptionsAreNotIgnoredInOverloadedMethod() throws Throwable {
        EventStoreDBClient client = getDefaultClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .notResolveLinkTos();

        List<ResolvedEvent> events1 = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", options))
                .filter(ReadMessage::hasEvent)
                .map(ReadMessage::getEvent)
                .collect(toList())
                .blockingGet();

        List<ResolvedEvent> events2 = Flowable.fromPublisher(client.readStreamReactive("dataset20M-1800", options))
                .filter(ReadMessage::hasEvent)
                .map(ReadMessage::getEvent)
                .collect(toList())
                .blockingGet();

        RecordedEvent firstEvent1 = events1.get(0).getOriginalEvent();
        RecordedEvent firstEvent2 = events2.get(0).getOriginalEvent();

        Assertions.assertEquals(events1.size(), events2.size());
        Assertions.assertEquals(firstEvent1.getEventId(), firstEvent2.getEventId());
    }
}

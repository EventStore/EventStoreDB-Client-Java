package com.eventstore.dbclient.expectations;

import com.eventstore.dbclient.*;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public interface ReadStreamTests extends Expectations {
    @Test
    default void testReadStreamForward10EventsFromPositionStart() throws Throwable {
        Assumptions.assumeFalse(getDatabase().isTargetingBelowOrEqual21_10());

        EventStoreDBClient client = getDefaultClient();

        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .maxCount(10)
                .notResolveLinkTos();

        ReadResult result = client.readStream("dataset20M-1800", options)
                .get();

        verifyAgainstTestData(
                result.getEvents(),
                "dataset20M-1800-e0-e10");
    }

    @Test
    default void testReadStreamBackward10EventsFromPositionEnd() throws Throwable {
        Assumptions.assumeFalse(getDatabase().isTargetingBelowOrEqual21_10());

        EventStoreDBClient client = getDefaultClient();
        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .maxCount(10)
                .notResolveLinkTos();

        ReadResult result = client.readStream("dataset20M-1800", options)
                .get();

        verifyAgainstTestData(
                result.getEvents(),
                "dataset20M-1800-e1999-e1990");
    }

    @Test
    default void testReadStreamOptionsAreNotIgnoredInOverloadedMethod() throws Throwable {
        EventStoreDBClient client = getDefaultClient();

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
    default void testStreamPositionWhenReading() throws Throwable {
        EventStoreDBClient client = getDefaultClient();

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
}

package com.eventstore.dbclient.expectations;

import com.eventstore.dbclient.*;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

public interface ReadAllReactiveTests extends Expectations {
    @Test
    default void testReadAllEventsReactiveForwardFromZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getDefaultClient();

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
    default void testReadAllEventsReactiveForwardFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getDefaultClient();

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
    default void testReadAllEventsReactiveBackwardsFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getDefaultClient();

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
}

package com.eventstore.dbclient.samples.appending_events;

import com.eventstore.dbclient.*;
import com.eventstore.dbclient.samples.TestEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AppendingEvents {
    private static void appendToStream(EventStoreDBClient client) throws ExecutionException, InterruptedException {
        // region append-to-stream
        EventData eventData = EventData
                .builderAsJson(
                        UUID.randomUUID(),
                        "some-event",
                        new TestEvent(
                                "1",
                                "some value"
                        ))
                .build();

        AppendToStreamOptions options = AppendToStreamOptions.get()
                .expectedRevision(ExpectedRevision.NO_STREAM);

        client.appendToStream("some-stream", options, eventData)
                .get();
        // endregion append-to-stream
    }

    private static void appendWithSameId(EventStoreDBClient client) throws ExecutionException, InterruptedException {
        // region append-duplicate-event
        EventData eventData = EventData
                .builderAsJson(
                        UUID.randomUUID(),
                        "some-event",
                        new TestEvent(
                                "1",
                                "some value"
                        ))
                .build();

        AppendToStreamOptions options = AppendToStreamOptions.get()
                .expectedRevision(ExpectedRevision.ANY);

        client.appendToStream("same-event-stream", options, eventData)
                .get();

        // attempt to append the same event again
        client.appendToStream("same-event-stream", options, eventData)
                .get();
        // endregion append-duplicate-event
    }

    private static void appendWithNoStream(EventStoreDBClient client) throws ExecutionException, InterruptedException {
        // region append-with-no-stream
        EventData eventDataOne = EventData
                .builderAsJson(
                        UUID.randomUUID(),
                        "some-event",
                        new TestEvent(
                                "1",
                                "some value"
                        ))
                .build();

        EventData eventDataTwo = EventData
                .builderAsJson(
                        UUID.randomUUID(),
                        "some-event",
                        new TestEvent(
                                "2",
                                "some other value"
                        ))
                .build();

        AppendToStreamOptions options = AppendToStreamOptions.get()
                .expectedRevision(ExpectedRevision.NO_STREAM);

        client.appendToStream("no-stream-stream", options, eventDataOne)
                .get();

        // attempt to append the same event again
        client.appendToStream("no-stream-stream", options, eventDataTwo)
                .get();
        // endregion append-with-no-stream
    }

    private static void appendWithConcurrencyCheck(EventStoreDBClient client) throws ExecutionException, InterruptedException {
        // region append-with-concurrency-check

        ReadStreamOptions readStreamOptions = ReadStreamOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readStream("concurrency-stream", readStreamOptions)
                .get();

        List<ResolvedEvent> events = result.getEvents();
        ResolvedEvent lastEvent = events.get(events.size() - 1);
        StreamRevision revision = lastEvent.getEvent().getStreamRevision();

        EventData clientOneData = EventData
                .builderAsJson(
                        UUID.randomUUID(),
                        "some-event",
                        new TestEvent(
                                "1",
                                "clientOne"
                        ))
                .build();

        EventData clientTwoData = EventData
                .builderAsJson(
                        UUID.randomUUID(),
                        "some-event",
                        new TestEvent(
                                "2",
                                "clientTwo"
                        ))
                .build();


        AppendToStreamOptions options = AppendToStreamOptions.get()
                .expectedRevision(revision);

        client.appendToStream("concurrency-stream", options, clientOneData)
                .get();

        client.appendToStream("concurrency-stream", options, clientTwoData)
                .get();
        // endregion append-with-concurrency-check
    }

    public void appendOverridingUserCredentials(EventStoreDBClient client) throws ExecutionException, InterruptedException {
        EventData eventData = EventData
                .builderAsJson(
                        UUID.randomUUID(),
                        "some-event",
                        new TestEvent(
                                "1",
                                "some value"
                        ))
                .build();
        //region overriding-user-credentials
        UserCredentials credentials = new UserCredentials("admin", "changeit");

        AppendToStreamOptions options = AppendToStreamOptions.get()
                .authenticated(credentials);

        client.appendToStream("some-stream", options, eventData)
                .get();
        // endregion overriding-user-credentials
    }
}

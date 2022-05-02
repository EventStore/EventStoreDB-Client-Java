package com.eventstore.dbclient.samples.reading_events;

import com.eventstore.dbclient.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReadingEvents {
    private static void readFromStream(EventStoreDBClient client) throws ExecutionException, InterruptedException, JsonProcessingException {
        // region read-from-stream
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart();

        List<ResolvedEvent> events = client.readStream("some-stream", options)
                .get();

        // endregion read-from-stream

        // region iterate-stream
        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
        // endregion iterate-stream
    }

    private static void readFromStreamPosition(EventStoreDBClient client) throws ExecutionException, InterruptedException, JsonProcessingException {
        // region read-from-stream-position
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromRevision(10)
                .maxCount(20);

        List<ResolvedEvent> events = client.readStream("some-stream", options)
                .get();

        // endregion read-from-stream-position

        // region iterate-stream
        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
        // endregion iterate-stream
    }

    private static void readStreamOverridingUserCredentials(EventStoreDBClient client) throws ExecutionException, InterruptedException {
        // region overriding-user-credentials
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .authenticated("admin", "changeit");

        List<ResolvedEvent> result = client.readStream("some-stream", options)
                .get();
        // endregion overriding-user-credentials
    }

    private static void readFromStreamPositionCheck(EventStoreDBClient client) throws JsonProcessingException, InterruptedException {
        // region checking-for-stream-presence
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromRevision(10)
                .maxCount(20);

        List<ResolvedEvent> events = null;
        try {
            events = client.readStream("some-stream", options)
                    .get();
        } catch (ExecutionException e) {
            Throwable innerException = e.getCause();

            if (innerException instanceof StreamNotFoundException) {
                return;
            }
        }

        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
        // endregion checking-for-stream-presence
    }

    private static void readFromStreamBackwards(EventStoreDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region reading-backwards
        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd();

        List<ResolvedEvent> events = client.readStream("some-stream", options)
                .get();

        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
        // endregion reading-backwards
    }

    private static void readFromAllStream(EventStoreDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart();

        List<ResolvedEvent> events = client.readAll(options)
                .get();

        // endregion read-from-all-stream

        // region read-from-all-stream-iterate
        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
        // endregion read-from-all-stream-iterate
    }

    private static void readAllOverridingUserCredentials(EventStoreDBClient client) throws ExecutionException, InterruptedException {
        // region read-all-overriding-user-credentials
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .authenticated("admin", "changeit");

        List<ResolvedEvent> result = client.readAll(options)
                .get();
        // endregion read-all-overriding-user-credentials
    }

    private static void ignoreSystemEvents(EventStoreDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region ignore-system-events
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart();

        List<ResolvedEvent> events = client.readAll(options)
                .get();

        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            if (recordedEvent.getEventType().startsWith("$")) {
                continue;
            }
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
        // endregion ignore-system-events
    }

    private static void readFromAllStreamBackwards(EventStoreDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream-backwards
        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromEnd();

        List<ResolvedEvent> events = client.readAll(options)
                .get();

        // endregion read-from-all-stream-backwards

        // region read-from-all-stream-iterate
        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
        // endregion read-from-all-stream-iterate
    }

    private static void filteringOutSystemEvents(EventStoreDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart();

        List<ResolvedEvent> events = client.readAll(options)
                .get();

        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            if (!recordedEvent.getEventType().startsWith("$")) {
                continue;
            }
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
    }

    private static void readFromStreamResolvingLinkTos(EventStoreDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream-resolving-link-Tos
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .resolveLinkTos();

        List<ResolvedEvent> events = client.readAll(options)
                .get();

        // endregion read-from-all-stream-resolving-link-Tos
        for (ResolvedEvent resolvedEvent : events) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
    }
}

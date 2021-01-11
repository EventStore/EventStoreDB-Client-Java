package com.eventstore.dbclient.samples;

import com.eventstore.dbclient.*;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class GetStarted {
    public static void Run() throws ParseError, ExecutionException, InterruptedException {
        // region createClient
        EventStoreDBClientSettings settings = EventStoreDBConnectionString.parse("{connectionString}");
        EventStoreDBClient client = EventStoreDBClient.create(settings);
        // region createClient

        // region createEvent
        TestEvent event = new TestEvent();
        event.setEntityId(UUID.randomUUID().toString());
        event.setImportantData("I wrote my first event!");

        EventData eventData = EventData
                .builderAsJson("TestEvent", event)
                .build();
        // endregion createEvent

        // region appendEvents
        WriteResult writeResult = client.appendToStream("some-stream", eventData)
                .get();
        // endregion appendEvents

        // region overriding-user-credentials
        AppendToStreamOptions options = AppendToStreamOptions.get()
                .authenticated(new UserCredentials("admin", "changeit"));

        client.appendToStream("some-stream", options, eventData)
                .get();
        // endregion overriding-user-credentials
    }
}

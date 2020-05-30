package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;
import testcontainers.module.EventStoreTestDBContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReadStreamTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    @Rule
    public final EventStoreStreamsClient client = new EventStoreStreamsClient(server);

    @Test
    public void testReadStreamForwards10Events() throws Throwable {
        CompletableFuture<ReadStreamResult> future = client.instance.readStream(
                Direction.Forward,
                "dataset20M-1800",
                StreamRevision.START,
                10,
                false);
        ReadStreamResult result = future.get();
        ResolvedEvent[] actualEvents = result.getEvents().toArray(new ResolvedEvent[0]);

        assertNotNull(result.getEvents());
        assertEquals(10, result.getEvents().size());

        TestResolvedEvent[] expectedEvents = loadSerializedTestData("dataset20M-1800-e0-e10");
        for (int i = 0; i < expectedEvents.length; i++) {
            TestResolvedEvent expected = expectedEvents[i];
            ResolvedEvent actual = actualEvents[i];

            expected.assertEquals(actual);
        }
    }

    private TestResolvedEvent[] loadSerializedTestData(String filenameStem) {
        String filename = String.format("%s.json", filenameStem);

        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);

        JsonMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        try {
            return mapper.readValue(stream, TestResolvedEvent[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

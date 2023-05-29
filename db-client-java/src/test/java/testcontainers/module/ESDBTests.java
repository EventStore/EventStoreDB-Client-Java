package testcontainers.module;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventDataBuilder;
import com.github.javafaker.Faker;
import extensions.BeforeEverythingElseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.UUID;

public class ESDBTests {
    private final Faker faker = new Faker();

    public static EventStoreDB getEmptyServer() {
        return BeforeEverythingElseExtension.GetEmptyServer();
    }

    public static EventStoreDB getPopulatedServer() {
        return BeforeEverythingElseExtension.GetPopulatedServer();
    }

    public static EventStoreDB getSecureServer() {
        return BeforeEverythingElseExtension.GetSecureServer();
    }

    protected String generateName() {
        return String.format("%s-%s", faker.pokemon().name(), faker.pokemon().name());
    }

    protected ArrayList<EventData> generateEvents(int amount, String eventType) {
        ArrayList<EventData> events = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            events.add(EventDataBuilder.json(UUID.randomUUID(), eventType, new byte[]{}).build());
        }

        return events;
    }
}

package testcontainers.module;

import com.eventstore.dbclient.*;
import com.github.dockerjava.api.model.HealthCheck;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class EventStoreDB extends GenericContainer<EventStoreDB> {
    public static final String NAME;
    public static final String IMAGE;
    public static final String IMAGE_TAG;
    public static final HealthCheck HEALTH_CHECK;
    private static final int DB_HTTP_PORT;

    static {
        NAME = "testdata";
        IMAGE = "ghcr.io/eventstore/" + NAME;
        IMAGE_TAG = "ci";
        HEALTH_CHECK = new HealthCheck()
                .withInterval(1000000000L)
                .withTimeout(1000000000L)
                .withRetries(10);
        DB_HTTP_PORT = 2113;
    }

    private static String getImageName() {
        String tag = Optional.ofNullable(System.getenv("EVENTSTORE_DOCKER_TAG_ENV")).orElse(IMAGE_TAG);
        return Optional.ofNullable(System.getenv("EVENTSTORE_IMAGE"))
                .orElse(IMAGE + ":" + tag);
    }

    public static boolean isTestedAgainstVersion20() {
        return Optional.ofNullable(System.getenv("EVENTSTORE_DOCKER_TAG_ENV")).orElse(IMAGE_TAG).startsWith("20");
    }

    public static boolean isTestedAgains20_10() {
        return Optional.ofNullable(System.getenv("EVENTSTORE_DOCKER_TAG_ENV")).orElse(IMAGE_TAG).startsWith("20.10");
    }

    private final EventStoreDBClientSettings settings;
    private final EventStoreDBClient client;
    private final EventStoreDBPersistentSubscriptionsClient persistentSubscriptionsClient;
    private final EventStoreDBProjectionManagementClient projectionClient;

    public EventStoreDB(boolean emptyDatabase) {
        super(getImageName());

        addExposedPorts(1113, 2113);

        withEnv("EVENTSTORE_RUN_PROJECTIONS", "ALL");

        withEnv("EVENTSTORE_INSECURE", "true");
        if (!emptyDatabase) {
            withEnv("EVENTSTORE_MEM_DB", "false");
            withEnv("EVENTSTORE_DB", "/data/integration-tests");
        }

        withCreateContainerCmdModifier(cmd -> cmd.withHealthcheck(HEALTH_CHECK));
        waitingFor(Wait.forHealthcheck());

        start();

        settings = getEventStoreDBClientSettings();
        client = EventStoreDBClient.create(settings);
        persistentSubscriptionsClient = EventStoreDBPersistentSubscriptionsClient.create(settings);
        projectionClient = EventStoreDBProjectionManagementClient.create(settings);
    }

    private EventStoreDBClientSettings getEventStoreDBClientSettings() {
        return EventStoreDBConnectionString.parseOrThrow(getConnectionString());
    }

    private String getConnectionString() {
        final String address = getContainerIpAddress();
        final int port = getMappedPort(DB_HTTP_PORT);

        return String.format("esdb://%s:%d?tls=false&defaultdeadline=60000", address, port);
    }

    public EventStoreDBClientSettings getSettings() {
        return settings;
    }

    public EventStoreDBClient getClient() {
        return client;
    }

    public EventStoreDBClient getClientWithSettings(String args) {
        final String address = getContainerIpAddress();
        final int port = getMappedPort(DB_HTTP_PORT);

        String connStr =  String.format("esdb://%s:%d?%s", address, port, args);
        return EventStoreDBClient.create(EventStoreDBConnectionString.parseOrThrow(connStr));
    }

    public EventStoreDBPersistentSubscriptionsClient getPersistentSubscriptionsClient() {
        return persistentSubscriptionsClient;
    }

    public EventStoreDBProjectionManagementClient getProjectionClient() {
        return projectionClient;
    }

    public void shutdownClients() {
        try {
            client.shutdown();
            persistentSubscriptionsClient.shutdown();
            projectionClient.shutdown();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

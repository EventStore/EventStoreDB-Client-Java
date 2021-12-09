package testcontainers.module;

import com.eventstore.dbclient.*;
import com.github.dockerjava.api.model.HealthCheck;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.List;
import java.util.Optional;

public class EventStoreTestDBContainer extends GenericContainer<EventStoreTestDBContainer> {
    public static final String NAME;
    public static final String IMAGE;
    public static final String IMAGE_TAG;
    public static final HealthCheck HEALTH_CHECK;
    private static final int DB_HTTP_PORT;

    private final boolean runProjections;

    static {
        NAME = "eventstore-client-grpc-testdata";
        IMAGE = "ghcr.io/eventstore/eventstore-client-grpc-testdata/" + NAME;
        IMAGE_TAG = "21.6.0-buster-slim";
        HEALTH_CHECK = new HealthCheck()
                .withInterval(1000000000L)
                .withTimeout(1000000000L)
                .withRetries(10);
        DB_HTTP_PORT = 2113;
    }

    private static String getImageName() {
        return Optional.ofNullable(System.getenv("EVENTSTORE_IMAGE"))
                .orElse(IMAGE + ":" + IMAGE_TAG);
    }

    public EventStoreTestDBContainer() {
        this(true);
    }

    public EventStoreTestDBContainer(boolean emptyDatabase) {
        this(getImageName(), emptyDatabase);
    }

    public EventStoreTestDBContainer(boolean emptyDatabase, boolean runProjections) {
        this(getImageName(), emptyDatabase, runProjections);
    }

    public EventStoreTestDBContainer(String image, boolean emptyDatabase) {
        this(image, emptyDatabase, false);
    }

    public EventStoreTestDBContainer(String image, boolean emptyDatabase, boolean runProjections) {
        super(image);

        this.runProjections = runProjections;

        addExposedPorts(1113, 2113);

        if(runProjections) {
            withEnv("EVENTSTORE_RUN_PROJECTIONS", "ALL");
        }

        withEnv("EVENTSTORE_INSECURE", "true");
        if (!emptyDatabase) {
            withEnv("EVENTSTORE_MEM_DB", "false");
            withEnv("EVENTSTORE_DB", "/data/integration-tests");
        }

        withCreateContainerCmdModifier(cmd -> cmd.withHealthcheck(HEALTH_CHECK));
        waitingFor(Wait.forHealthcheck());
    }

    public EventStoreDBClient getClient() {
        final EventStoreDBClientSettings settings = getEventStoreDBClientSettings();

        return EventStoreDBClient.create(settings);
    }

    public EventStoreDBPersistentSubscriptionsClient getPersistentSubscriptionsClient() {
        final EventStoreDBClientSettings settings = getEventStoreDBClientSettings();

        return EventStoreDBPersistentSubscriptionsClient.create(settings);
    }

    public EventStoreDBProjectionManagementClient getProjectionManagementClient() {
        final EventStoreDBClientSettings settings = getEventStoreDBClientSettings();

        return EventStoreDBProjectionManagementClient.create(settings);
    }

    private EventStoreDBClientSettings getEventStoreDBClientSettings() {
        return EventStoreDBConnectionString.parseOrThrow(getConnectionString());
    }

    public String getConnectionString() {
        final String address = getContainerIpAddress();
        final int port = getMappedPort(DB_HTTP_PORT);

        return String.format("esdb://%s:%d?tls=false", address, port);
    }

    public void waitForInitialization() {
        waitForStreamInitialization("$users", 2);

        if(runProjections){
            waitForStreamInitialization("$projections-$all", 6);
        }
    }

    private void waitForStreamInitialization(String stream, int requiredCount){
        while(true){
            try {
                EventStoreDBClient client = getClient();
                ReadResult result = client.readStream(stream).get();
                List<ResolvedEvent> events = result.getEvents();

                if(events.size() >= requiredCount){
                    break;
                }
            } catch (Exception ignored) {
            }

            try {
                Thread.sleep(500);
            } catch (Exception ex) {
                return;
            }
        }
    }
}

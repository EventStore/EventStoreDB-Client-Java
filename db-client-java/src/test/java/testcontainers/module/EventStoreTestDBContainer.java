package testcontainers.module;

import com.eventstore.dbclient.*;
import com.github.dockerjava.api.model.HealthCheck;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class EventStoreTestDBContainer extends GenericContainer<EventStoreTestDBContainer> {
    public static final String NAME;
    public static final String IMAGE;
    public static final String IMAGE_TAG;
    public static final HealthCheck HEALTH_CHECK;
    private static final int DB_HTTP_PORT;

    static {
        NAME = "eventstore-client-grpc-testdata";
        IMAGE = "docker.pkg.github.com/eventstore/eventstore-client-grpc-testdata/" + NAME;
        IMAGE_TAG = "20.6.1-buster-slim";
        HEALTH_CHECK = new HealthCheck()
                .withInterval(1000000000L)
                .withTimeout(1000000000L)
                .withRetries(10);
        DB_HTTP_PORT = 2113;
    }

    public EventStoreTestDBContainer() {
        this(true);
    }

    public EventStoreTestDBContainer(boolean emptyDatabase) {
        this(IMAGE + ":" + IMAGE_TAG, emptyDatabase);
    }

    public EventStoreTestDBContainer(boolean emptyDatabase, boolean runProjections) {
        this(IMAGE + ":" + IMAGE_TAG, emptyDatabase, runProjections);
    }

    public EventStoreTestDBContainer(String image, boolean emptyDatabase) {
        this(image, emptyDatabase, false);
    }

    public EventStoreTestDBContainer(String image, boolean emptyDatabase, boolean runProjections) {
        super(image);

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

    public PersistentSubscriptions getPersistentSubscriptionsAPI() {
        return getClient().persistentSubscriptions();
    }

    public EventStoreDBProjectionManagementClient getProjectionManagementClient() {
        final EventStoreDBClientSettings settings = getEventStoreDBClientSettings();

        return EventStoreDBProjectionManagementClient.create(settings);
    }

    private EventStoreDBClientSettings getEventStoreDBClientSettings() {
        final String address = getContainerIpAddress();
        final int port = getMappedPort(DB_HTTP_PORT);
        final EventStoreDBClientSettings settings = ConnectionString.parseOrThrow(String.format("esdb://%s:%d?tls=false", address, port));
        return settings;
    }

}

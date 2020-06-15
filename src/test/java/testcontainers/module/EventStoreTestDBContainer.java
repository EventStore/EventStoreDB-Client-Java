package testcontainers.module;

import com.eventstore.dbclient.StreamsClient;
import com.eventstore.dbclient.Timeouts;
import com.eventstore.dbclient.UserCredentials;
import com.github.dockerjava.api.model.HealthCheck;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.net.ssl.SSLException;

public class EventStoreTestDBContainer extends GenericContainer<EventStoreTestDBContainer> {
    public static final String NAME;
    public static final String IMAGE;
    public static final String IMAGE_TAG;
    public static final HealthCheck HEALTH_CHECK;
    private static final int DB_HTTP_PORT;

    static {
        NAME = "eventstore-client-grpc-testdata";
        IMAGE = "docker.pkg.github.com/eventstore/eventstore-client-grpc-testdata/" + NAME;
        IMAGE_TAG = "20.6.0-buster-slim";
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

    public EventStoreTestDBContainer(String image, boolean emptyDatabase) {
        super(image);

        addExposedPorts(1113, 2113);

        withEnv("EVENTSTORE_DEV", "true");
        if (!emptyDatabase) {
            withEnv("EVENTSTORE_MEM_DB", "false");
            withEnv("EVENTSTORE_DB", "/data/integration-tests");
        }

        withCreateContainerCmdModifier(cmd -> cmd.withHealthcheck(HEALTH_CHECK));
        waitingFor(Wait.forHealthcheck());
    }

    public StreamsClient getStreamsClient() {
        final String address = getContainerIpAddress();
        final int port = getMappedPort(DB_HTTP_PORT);
        final SslContext sslContext = getClientSslContext();
        final UserCredentials creds = new UserCredentials("admin", "changeit");
        final Timeouts timeouts = Timeouts.DEFAULT;

        return new StreamsClient(address, port, creds, timeouts, sslContext);
    }

    private SslContext getClientSslContext() {
        try {
            return GrpcSslContexts.
                    forClient().
                    trustManager(InsecureTrustManagerFactory.INSTANCE).
                    build();
        } catch (SSLException ex) {
            return null;
        }
    }
}

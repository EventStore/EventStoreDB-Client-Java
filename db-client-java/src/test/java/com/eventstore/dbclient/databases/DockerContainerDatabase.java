package com.eventstore.dbclient.databases;

import com.eventstore.dbclient.*;
import com.github.dockerjava.api.model.HealthCheck;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class DockerContainerDatabase extends GenericContainer<DockerContainerDatabase> implements Database {
    private static final String REGISTRY;
    private static final HealthCheck HEALTH_CHECK;

    static {
        REGISTRY = "ghcr.io/eventstore";
        HEALTH_CHECK = new HealthCheck()
                .withInterval(1000000000L)
                .withTimeout(1000000000L)
                .withRetries(10);
    }

    public static class Builder {
        String image;
        String version;
        boolean secure;
        boolean anonymous;

        public Builder() {
            this.image = "eventstore";
            this.version = "latest";
        }

        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder anonymous(boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        public Builder grpcTestDataImage() {
            return this.image("testdata");
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public DockerContainerDatabase build() {
            return new DockerContainerDatabase(this);
        }
    }

    private final Builder builder;
    private final ClientTracker clientTracker;

    public DockerContainerDatabase(Builder builder) {
        super(String.format("%s/%s:%s", REGISTRY, builder.image, builder.version));
        addExposedPorts(1113, 2113);

        withEnv("EVENTSTORE_RUN_PROJECTIONS", "ALL");
        withEnv("EVENTSTORE_ENABLE_ATOM_PUB_OVER_HTTP", "true");

        if (builder.secure) {
            verifyCertificatesExist();
            String certsDir = Paths.get(System.getProperty("user.dir"), "..", "certs").toAbsolutePath().toString();

            withEnv("EVENTSTORE_CERTIFICATE_FILE", "/etc/eventstore/certs/node/node.crt");
            withEnv("EVENTSTORE_CERTIFICATE_PRIVATE_KEY_FILE", "/etc/eventstore/certs/node/node.key");
            withEnv("EVENTSTORE_TRUSTED_ROOT_CERTIFICATES_PATH", "/etc/eventstore/certs/ca");
            withFileSystemBind(certsDir, "/etc/eventstore/certs");
        } else {
            withEnv("EVENTSTORE_INSECURE", "true");
        }

        this.builder = builder;
        this.clientTracker = new ClientTracker();

        withCreateContainerCmdModifier(cmd -> cmd.withHealthcheck(HEALTH_CHECK));
        waitingFor(Wait.forHealthcheck());

        start();
    }

    @Override
    public ConnectionSettingsBuilder defaultSettingsBuilder() {
        ConnectionSettingsBuilder settingsBuilder = EventStoreDBClientSettings.builder().addHost(getHost(), getMappedPort(2113));

        if (!builder.anonymous)
            settingsBuilder.defaultCredentials("admin", "changeit");

        if (builder.secure)
            settingsBuilder.tls(true).tlsVerifyCert(false);
        else
            settingsBuilder.tls(false);

        return settingsBuilder;
    }

    @Override
    public ClientTracker getClientTracker() {
        return clientTracker;
    }

    @Override
    public void cleanup() {
        try {
            ExecResult result = execInContainer("tar", "-czvf", "/tmp/esdb_logs.tar.gz", "/var/log/eventstore");
            if (result.getExitCode() != 0) {
                logger().error(result.getStderr());
                throw new RuntimeException("Error when compressing server logs");
            }
            copyFileFromContainer("/tmp/esdb_logs.tar.gz", "./esdb_logs.tar.gz");
        } catch (Exception e) {
            logger().error("Error when cleanup docker container", e);
        } finally {
            stop();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private static void verifyCertificatesExist() {
        String currentDir = System.getProperty("user.dir");
        String[][] files =  {
                 { "ca", "ca.crt" },
                { "ca", "ca.key" },
                { "node", "node.crt" },
                { "node", "node.key" },
        };

        for (String[] strings : files) {
            File file = Paths.get(currentDir, "..", "certs", strings[0], strings[1]).toAbsolutePath().toFile();

            if (!file.exists())
                throw new RuntimeException(new FileNotFoundException(file.getAbsolutePath()));
        }
    }
}

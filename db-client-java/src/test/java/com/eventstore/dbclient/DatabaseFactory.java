package com.eventstore.dbclient;

import com.eventstore.dbclient.databases.DockerContainerDatabase;
import com.eventstore.dbclient.databases.ExternallyCreatedCluster;

import java.util.Optional;

public class DatabaseFactory {
    public static Database spawn() {
        boolean secure = Boolean.parseBoolean(Optional.ofNullable(System.getenv("SECURE")).orElse("false"));
        boolean cluster = Boolean.parseBoolean(Optional.ofNullable(System.getenv("CLUSTER")).orElse("false"));

        if (cluster)
            return new ExternallyCreatedCluster(secure);

        return singleNodeBuilder()
                .secure(secure)
                .build();
    }

    public static Database spawnEnterpriseWithPluginsEnabled(String... pluginsToEnable) {
        boolean secure = Boolean.parseBoolean(Optional.ofNullable(System.getenv("SECURE")).orElse("false"));
        boolean cluster = Boolean.parseBoolean(Optional.ofNullable(System.getenv("CLUSTER")).orElse("false"));

        if (cluster)
            return new ExternallyCreatedCluster(secure);

        DockerContainerDatabase.Builder builder = singleNodeBuilder();

        for (String plugin : pluginsToEnable)
            builder.env(String.format("EventStore__Plugins__%s__Enabled", plugin), "true");

        return builder.secure(secure).build();
    }

    public static Database spawnPopulatedDatabase() {
        return singleNodeBuilder()
                .withTestData()
                .build();
    }

    private static DockerContainerDatabase.Builder singleNodeBuilder() {
        return DockerContainerDatabase
                .builder()
                .registry(Optional
                        .ofNullable(System.getenv("EVENTSTORE_DOCKER_REGISTRY_ENV"))
                        .orElse(DockerContainerDatabase.DEFAULT_REGISTRY))
                .image(Optional
                        .ofNullable(System.getenv("EVENTSTORE_DOCKER_IMAGE_ENV"))
                        .orElse(DockerContainerDatabase.DEFAULT_IMAGE))
                .version(Optional
                        .ofNullable(System.getenv("EVENTSTORE_DOCKER_TAG_ENV"))
                        .orElse(DockerContainerDatabase.DEFAULT_VERSION));
    }
}

package com.eventstore.dbclient.samples.authentication;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;

public class UserCertificate {
    private static void tracing() {
        // region client-with-user-certificates
        EventStoreDBClientSettings settings = EventStoreDBConnectionString
                .parseOrThrow("esdb://admin:changeit@{endpoint}?tls=true&userCertFile={pathToCaFile}&userKeyFile={pathToKeyFile}");
        EventStoreDBClient client = EventStoreDBClient.create(settings);
        // endregion client-with-user-certificates
    }
}

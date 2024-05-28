package com.eventstore.dbclient.samples.authentication;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;

public class UserCertificate {
    private static void tracing() {
        // region client-with-user-certificates
        EventStoreDBClientSettings settings = EventStoreDBClientSettings.builder()
                .tls(true)
                .defaultClientCertificate("/path/to/ca.crt", "/path/to/ca.key")
                .defaultCredentials("admin", "changeit")
                .buildConnectionSettings();

        EventStoreDBClient client = EventStoreDBClient.create(settings);
        // endregion client-with-user-certificates
    }
}

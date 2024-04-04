package com.eventstore.dbclient.plugins;

import com.eventstore.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public interface ClientCertificateAuthenticationTests extends ConnectionAware {
    @Test
    default void testClientCertificateAuthenticationWithValidCertificate() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultClientCertificate(ClientCertificate("admin"), userKey("admin"))
                            .defaultCredentials(null)
                            .buildConnectionSettings());

            client.readAll().get();
        });
    }

    static String ClientCertificate(String user) {
        return buildCertPath(user, "crt");
    }

    static String userKey(String user) {
        return buildCertPath(user, "key");
    }

    static String buildCertPath(String user, String extension) {
        String certsPath = Paths.get(System.getProperty("user.dir"), "..", "certs").toAbsolutePath().toString();
        return String.format("%s/user-%s/user-%s.%s", certsPath, user, user, extension);
    }
}

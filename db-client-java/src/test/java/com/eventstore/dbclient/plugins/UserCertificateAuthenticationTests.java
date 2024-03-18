package com.eventstore.dbclient.plugins;

import com.eventstore.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public interface UserCertificateAuthenticationTests extends ConnectionAware {
    String ValidUserUsername = "admin", ValidUserPassword = "changeit";
    String ValidUserCertificate = userCertificate(ValidUserUsername), ValidUserKey = userKey(ValidUserUsername);
    String InvalidUserUsername = "invalid", InvalidUserCertificate = userCertificate(InvalidUserUsername), InvalidUserKey = userKey(InvalidUserUsername);

    @Test
    default void testDefaultCredentialsOnly() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultCredentials(ValidUserUsername, ValidUserPassword)
                            .defaultUserCertificate(null)
                            .buildConnectionSettings());

            client.readAll().get();
        });
    }

    @Test
    default void testDefaultCertificateOnly() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultCredentials(null)
                            .defaultUserCertificate(ValidUserCertificate, ValidUserKey)
                            .buildConnectionSettings());

            client.readAll().get();
        });
    }

    @Test
    default void testOverrideCredentialsOnly() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultCredentials(null)
                            .defaultUserCertificate(null)
                            .buildConnectionSettings());

            ReadAllOptions optionsWithOverrideCredentials = ReadAllOptions.get()
                    .authenticated(new UserCredentials(ValidUserUsername, ValidUserPassword));

            client.readAll(optionsWithOverrideCredentials).get();
        });
    }

    @Test
    default void testOverrideCertificateOnly() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultCredentials(null)
                            .defaultUserCertificate(null)
                            .buildConnectionSettings());

            ReadAllOptions optionsWithOverrideCertificate = ReadAllOptions.get()
                    .authenticated(new UserCertificate(ValidUserCertificate, ValidUserKey));

            client.readAll(optionsWithOverrideCertificate).get();
        });
    }

    /*
     * User credentials should take precedence over a user certificate,
     * The below tests would fail with an unauthenticated error from
     * the server if the user certificate was used in any case.
     */

    @Test
    default void testDefaultCredentialsAndDefaultCertificate() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultCredentials(ValidUserUsername, ValidUserPassword)
                            .defaultUserCertificate(InvalidUserCertificate, InvalidUserKey)
                            .buildConnectionSettings());

            client.readAll().get();
        });
    }

    @Test
    default void testOverrideCredentialsAndOverrideCertificate() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultCredentials(null)
                            .defaultUserCertificate(null)
                            .buildConnectionSettings());

            ReadAllOptions optionsWithOverrideCredentials = ReadAllOptions.get()
                    .authenticated(new UserCredentials(ValidUserUsername, ValidUserPassword))
                    .authenticated(new UserCertificate(InvalidUserCertificate, InvalidUserKey));

            client.readAll(optionsWithOverrideCredentials).get();
        });
    }

    @Test
    default void testDefaultCredentialsAndOverrideCertificate() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultCredentials(ValidUserUsername, ValidUserPassword)
                            .defaultUserCertificate(null)
                            .buildConnectionSettings());

            ReadAllOptions optionsWithOverrideCredentials = ReadAllOptions.get()
                    .authenticated(new UserCertificate(InvalidUserCertificate, InvalidUserKey));

            client.readAll(optionsWithOverrideCredentials).get();
        });
    }

    @Test
    default void testDefaultCertificateAndOverrideCredentials() {
        Assertions.assertDoesNotThrow(() -> {
            EventStoreDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultCredentials(null)
                            .defaultUserCertificate(InvalidUserCertificate, InvalidUserKey)
                            .buildConnectionSettings());

            ReadAllOptions optionsWithOverrideCredentials = ReadAllOptions.get()
                    .authenticated(new UserCredentials(ValidUserUsername, ValidUserPassword));

            client.readAll(optionsWithOverrideCredentials).get();
        });
    }

    static String userCertificate(String user) {
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

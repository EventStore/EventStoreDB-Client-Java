package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

public class EventStoreDBUserManagementClientTest extends ESDBTests {

    @Test
    public void shouldCreateUser() throws Exception {
        // Given
        EventStoreDBUserManagementClient client = getSecureServer().getUserManagementClient();
        String loginName = randomUUID().toString();
        String fullName = "satoshi nakamoto";
        String password = "changeit";
        List<String> groups = asList("finance", "hr");

        // When
        client.create(loginName, fullName, password, groups).get();

        // Then
        User user = client.details(loginName).get();
        assertEquals(user.loginName(), loginName);
        assertEquals(user.fullName(), fullName);
        assertEquals(user.groups(), groups);
        assertFalse(user.isDisabled());
    }

    @Test
    public void shouldUpdateUser() throws Exception {
        // Given
        EventStoreDBUserManagementClient client = getSecureServer().getUserManagementClient();
        String loginName = randomUUID().toString();
        client.create(loginName, "satoshi nakamoto", "changeit", asList("finance", "hr")).get();
        Instant createdAt = client.details(loginName).get().lastUpdated();

        // When
        client.update(loginName, "updated name", "updatedpass", singletonList("$admins")).get();

        // Then
        User user = client.details(loginName).get();
        assertEquals(user.loginName(), loginName);
        assertEquals(user.fullName(), "updated name");
        assertEquals(user.groups(), singletonList("$admins"));
        assertFalse(user.isDisabled());
        assertTrue(user.lastUpdated().isAfter(createdAt));
    }


    @Test
    public void shouldDisableAndEnableUser() throws Exception {
        // Given
        EventStoreDBUserManagementClient client = getSecureServer().getUserManagementClient();
        String loginName = randomUUID().toString();
        client.create(loginName, "satoshi nakamoto", "changeit", singletonList("hr")).get();
        client.disable(loginName).get();

        // When
        client.disable(loginName).get();

        // Then
        assertTrue(client.details(loginName).get().isDisabled());

        // When
        client.enable(loginName).get();

        // Then
        assertFalse(client.details(loginName).get().isDisabled());
    }

    @Test
    public void shouldChangePassword() throws Exception {
        // Given
        EventStoreDBUserManagementClient client = getSecureServer().getUserManagementClient();
        String loginName = randomUUID().toString();
        String fullName = "full name";
        String currentPassword = "changeit";
        client.create(loginName, fullName, currentPassword, singletonList("$admins")).get();

        // When
        String newPassword = "newpass";
        client.changePassword(loginName, currentPassword, newPassword).get();

        // Then
        assertTrue(client.details(loginName).get().lastUpdated().isBefore(now()));
    }

    @Test
    public void shouldResetPassword() throws Exception {
        // Given
        EventStoreDBUserManagementClient client = getSecureServer().getUserManagementClient();
        String loginName = randomUUID().toString();
        String fullName = "full name";
        String currentPassword = "changeit";
        client.create(loginName, fullName, currentPassword, singletonList("$admins")).get();

        // When
        String newPassword = "newpass";
        client.resetPassword(loginName, newPassword).get();

        // Then
        assertTrue(client.details(loginName).get().lastUpdated().isBefore(now()));
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        // Given
        EventStoreDBUserManagementClient client = getSecureServer().getUserManagementClient();
        String loginName = randomUUID().toString();
        client.create(loginName, "satoshi nakamoto", "changeit", singletonList("hr")).get();

        // When
        client.delete(loginName).get();

        // Then
        assertThrows(Exception.class, () -> client.details(loginName).get());
    }
}

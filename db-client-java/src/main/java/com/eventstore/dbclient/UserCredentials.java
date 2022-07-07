package com.eventstore.dbclient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Holds a login and a password for authenticated requests.
 */
public final class UserCredentials {
    private final String base64Encoded;

    public UserCredentials(String username, String password) {
        byte[] credentialsBytes = String.format("%s:%s", username, password).getBytes(StandardCharsets.US_ASCII);
        base64Encoded = String.format("Basic %s", Base64.getEncoder().encodeToString(credentialsBytes));
    }

    String basicAuthHeader() {
        return base64Encoded;
    }
}

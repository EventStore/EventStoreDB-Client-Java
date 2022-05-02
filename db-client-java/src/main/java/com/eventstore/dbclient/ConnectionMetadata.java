package com.eventstore.dbclient;

import io.grpc.Metadata;

class ConnectionMetadata {
    private Metadata metadata;

    public ConnectionMetadata() {
        this.metadata = new Metadata();
    }

    public ConnectionMetadata authenticated(UserCredentials credentials) {
        this.metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), credentials.basicAuthHeader());
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.containsKey(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
    }

    public String getUserCredentials() {
        return this.metadata.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
    }

    public ConnectionMetadata requiresLeader() {
        this.metadata.put(Metadata.Key.of("requires-leader", Metadata.ASCII_STRING_MARSHALLER), String.valueOf(true));
        return this;
    }

    public Metadata build() {
        return this.metadata;
    }
}

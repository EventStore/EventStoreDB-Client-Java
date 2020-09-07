package com.eventstore.dbclient;

public class Endpoint {
    private String hostname;
    private int port;

    public Endpoint(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}

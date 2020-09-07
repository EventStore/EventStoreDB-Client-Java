package com.eventstore.dbclient;

public class NotLeaderException extends Exception {
    private Endpoint leaderEndpoint;

    public NotLeaderException(String host, int port) {
        leaderEndpoint = new Endpoint(host, port);
    }

    public Endpoint getLeaderEndpoint() {
        return leaderEndpoint;
    }
}

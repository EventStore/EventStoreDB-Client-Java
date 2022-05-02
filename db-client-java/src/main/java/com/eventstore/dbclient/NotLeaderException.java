package com.eventstore.dbclient;

/**
 * When a request needing a leader node was executed on a follower node.
 * In this case the connection will reconnect automatically to the leader node. However, the request causing that
 * exception needs to be retried if the user really wants it to be carried out.
 */
public class NotLeaderException extends Exception {
    private Endpoint leaderEndpoint;

    NotLeaderException(String host, int port) {
        leaderEndpoint = new Endpoint(host, port);
    }

    public Endpoint getLeaderEndpoint() {
        return leaderEndpoint;
    }
}

package com.eventstore.dbclient;

public class ConnectionShutdownException extends Exception {
    public ConnectionShutdownException() {
        super("The connection is closed");
    }
}

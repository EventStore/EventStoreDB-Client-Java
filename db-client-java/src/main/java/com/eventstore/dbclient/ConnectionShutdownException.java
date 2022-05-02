package com.eventstore.dbclient;

/**
 * When a connection is already closed.
 */
public class ConnectionShutdownException extends Exception {
    ConnectionShutdownException() {
        super("The connection is closed");
    }
}

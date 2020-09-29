package com.eventstore.dbclient;

public final class Connections {
    public static ConnectionBuilder builder() {
        return new ConnectionBuilder();
    }
}

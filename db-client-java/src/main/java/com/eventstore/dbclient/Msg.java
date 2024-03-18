package com.eventstore.dbclient;

interface Msg {
    void accept(ConnectionService handler);
}

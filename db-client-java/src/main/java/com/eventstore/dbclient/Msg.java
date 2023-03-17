package com.eventstore.dbclient;

interface Msg {
    void accept(MsgHandler handler);
}

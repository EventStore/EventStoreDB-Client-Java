package com.eventstore.dbclient;

public enum ConsumerStrategy {
    DispatchToSingle,
    RoundRobin,
    Pinned,
}

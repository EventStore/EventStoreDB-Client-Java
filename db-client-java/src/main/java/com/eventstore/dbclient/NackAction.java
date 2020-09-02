package com.eventstore.dbclient;

public enum NackAction {
    Park,
    Retry,
    Skip,
    Stop,
}

package com.eventstore.dbclient;

/**
 * A request not supported by the targeted EventStoreDB node was sent.
 */
public class UnsupportedFeatureException extends RuntimeException {
    UnsupportedFeatureException(){}
}

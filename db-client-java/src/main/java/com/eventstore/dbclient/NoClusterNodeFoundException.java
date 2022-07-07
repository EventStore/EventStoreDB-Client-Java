package com.eventstore.dbclient;

/**
 * When no node was found based on the connection string provided.
 */
public class NoClusterNodeFoundException extends Exception {
    NoClusterNodeFoundException(){}
}

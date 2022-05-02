package com.eventstore.dbclient;

/**
 * When no node where found based on the connection string provided.
 */
public class NoClusterNodeFoundException extends Exception {
    NoClusterNodeFoundException(){}
}

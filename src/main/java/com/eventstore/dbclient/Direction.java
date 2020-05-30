package com.eventstore.dbclient;

/**
 * Specifies the direction of a read operation.
 */
public enum Direction {
    /**
     * Read in the forward direction.
     */
    Forward,
    /**
     * Read in the backward direction.
     */
    Backward
}

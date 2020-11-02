package com.eventstore.dbclient;

public class ParseError extends Throwable {
    public ParseError(String connectionString, int from, int to, String expected) {
        super("Unexpected " + connectionString.substring(from, from == to ? from + 1 : to) + " at position " + from + ", expected " + expected);
    }
}

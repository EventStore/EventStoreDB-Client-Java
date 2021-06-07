package com.eventstore.dbclient;

import java.util.Iterator;
import java.util.List;

public class ReadResult {
    private final Iterable<ResolvedEvent> iterable;

    public ReadResult(Iterable<ResolvedEvent> iterable) {
        this.iterable = iterable;
    }

    public Iterable<ResolvedEvent> getEvents() {
        return this.iterable;
    }
}

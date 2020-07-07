package com.eventstore.dbclient;

import java.util.List;

public class ReadResult {
    private final List<ResolvedEvent> events;

    public ReadResult(List<ResolvedEvent> events) {
        this.events = events;
    }

    public List<ResolvedEvent> getEvents() {
        return this.events;
    }
}

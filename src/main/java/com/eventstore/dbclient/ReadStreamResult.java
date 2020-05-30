package com.eventstore.dbclient;

import java.util.List;

public class ReadStreamResult {
    private final List<ResolvedEvent> events;

    public ReadStreamResult(List<ResolvedEvent> events) {
        this.events = events;
    }

    public List<ResolvedEvent> getEvents() {
        return this.events;
    }
}

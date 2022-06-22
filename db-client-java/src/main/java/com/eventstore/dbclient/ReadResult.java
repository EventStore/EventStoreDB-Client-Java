package com.eventstore.dbclient;

import java.util.List;

public class ReadResult {
    private final List<ResolvedEvent> events;
    private final long firstStreamPosition;
    private final long lastStreamPosition;
    private final Position lastAllStreamPosition;

    public ReadResult(List<ResolvedEvent> events, long firstStreamPosition, long lastStreamPosition, Position lastAllStreamPosition) {
        this.events = events;
        this.firstStreamPosition = firstStreamPosition;
        this.lastStreamPosition = lastStreamPosition;
        this.lastAllStreamPosition = lastAllStreamPosition;
    }

    public List<ResolvedEvent> getEvents() {
        return this.events;
    }

    public long getFirstStreamPosition() {
        return firstStreamPosition;
    }

    public long getLastStreamPosition() {
        return lastStreamPosition;
    }

    public Position getLastAllStreamPosition() {
        return lastAllStreamPosition;
    }
}

package com.eventstore.dbclient;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EventCollectorReadSubscriber extends ReadSubscriber {
    List<ResolvedEvent> events = new LinkedList<>();

    public List<ResolvedEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public void onEvent(ResolvedEvent resolvedEvent) {
        events.add(resolvedEvent);
    }

    @Override
    public void onError(Throwable error) {
    }

    @Override
    public void onComplete() {
    }
}

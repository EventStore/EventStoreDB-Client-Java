package com.eventstore.dbclient;

import java.util.ArrayList;
import java.util.List;

public class EventCollectorReadObserver  extends ReadObserver<List<ResolvedEvent>> {
    List<ResolvedEvent> events = new ArrayList<>();

    @Override
    public void onNext(ResolvedEvent event) {
        events.add(event);
    }

    @Override
    public List<ResolvedEvent> onCompleted() {
        return events;
    }

    @Override
    public void onError(Throwable error) {

    }
}

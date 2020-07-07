package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

public class ResolvedEvent {
    private final RecordedEvent event;
    private final RecordedEvent link;

    public ResolvedEvent(RecordedEvent event, RecordedEvent link) {
        this.event = event;
        this.link = link;
    }

    public RecordedEvent getEvent() {
        return event;
    }

    public RecordedEvent getLink() {
        return link;
    }

    static ResolvedEvent fromWire(StreamsOuterClass.ReadResp.ReadEvent wireEvent) {
        RecordedEvent event = wireEvent.hasEvent() ? RecordedEvent.fromWire(wireEvent.getEvent()) : null;
        RecordedEvent link = wireEvent.hasLink() ? RecordedEvent.fromWire(wireEvent.getLink()) : null;

        return new ResolvedEvent(event, link);
    }
}

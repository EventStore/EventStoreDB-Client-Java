package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

import java.util.Objects;
import java.util.StringJoiner;

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

    public RecordedEvent getOriginalEvent() {
        return this.link != null ? this.link : this.event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolvedEvent that = (ResolvedEvent) o;
        return event.equals(that.event) && Objects.equals(link, that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, link);
    }

    static ResolvedEvent fromWire(StreamsOuterClass.ReadResp.ReadEvent wireEvent) {
        RecordedEvent event = wireEvent.hasEvent() ? RecordedEvent.fromWire(wireEvent.getEvent()) : null;
        RecordedEvent link = wireEvent.hasLink() ? RecordedEvent.fromWire(wireEvent.getLink()) : null;

        return new ResolvedEvent(event, link);
    }

    static ResolvedEvent fromWire(Persistent.ReadResp.ReadEvent wireEvent) {
        RecordedEvent event = wireEvent.hasEvent() ? RecordedEvent.fromWire(wireEvent.getEvent()) : null;
        RecordedEvent link = wireEvent.hasLink() ? RecordedEvent.fromWire(wireEvent.getLink()) : null;

        return new ResolvedEvent(event, link);
    }
}

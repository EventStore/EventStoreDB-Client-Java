package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class ResolvedEvent {
    private final RecordedEvent event;
    private final RecordedEvent link;

    private final Position position;

    public ResolvedEvent(RecordedEvent event, RecordedEvent link, Position position) {
        this.event = event;
        this.link = link;
        this.position = position;
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

    /**
     * Returns the transaction log position of the resolved event.
     * @see Position
     */
    public Optional<Position> getPosition() {
        return Optional.ofNullable(position);
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
        Position position = wireEvent.hasNoPosition() ? null : new Position(wireEvent.getCommitPosition(), wireEvent.getCommitPosition());

        return new ResolvedEvent(event, link, position);
    }

    static ResolvedEvent fromWire(Persistent.ReadResp.ReadEvent wireEvent) {
        RecordedEvent event = wireEvent.hasEvent() ? RecordedEvent.fromWire(wireEvent.getEvent()) : null;
        RecordedEvent link = wireEvent.hasLink() ? RecordedEvent.fromWire(wireEvent.getLink()) : null;
        Position position = wireEvent.hasNoPosition() ? null : new Position(wireEvent.getCommitPosition(), wireEvent.getCommitPosition());


        return new ResolvedEvent(event, link, position);
    }
}

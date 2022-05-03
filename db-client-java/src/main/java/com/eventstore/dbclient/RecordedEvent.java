package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RecordedEvent {
    @NotNull
    private final String streamId;
    @NotNull
    private final StreamRevision streamRevision;
    @NotNull
    private final UUID eventId;
    @NotNull
    private final String eventType;
    @NotNull
    private final byte[] eventData;
    @NotNull
    private final byte[] userMetadata;
    @NotNull
    private final Instant created;
    @NotNull
    private final Position position;
    @NotNull
    private final String contentType;

    private final ObjectMapper mapper;

    public RecordedEvent(
            @NotNull String eventStreamId,
            @NotNull StreamRevision streamRevision,
            @NotNull UUID eventId,
            @NotNull Position position,
            @NotNull Map<String, String> systemMetadata,
            @NotNull byte[] eventData,
            @NotNull byte[] userMetadata
    ) {
        this.streamId = eventStreamId;
        this.streamRevision = streamRevision;
        this.eventId = eventId;
        this.position = position;
        this.eventData = eventData;
        this.userMetadata = userMetadata;
        this.eventType = systemMetadata.get(SystemMetadataKeys.TYPE);
        this.contentType = systemMetadata.get(SystemMetadataKeys.CONTENT_TYPE);
        this.created = systemMetadataDateToInstant(systemMetadata.get(SystemMetadataKeys.CREATED));
        this.mapper = new JacksonObjectMapperProvider(new JacksonObjectMapperFactoryImpl()).getOrCreate(this.getClass().getSimpleName(), null);
    }

    /**
     * Converts a timestamp in the form of .NET "Ticks" (100ns increments) since the UNIX Epoch
     * into a java.time.Instant;
     *
     * @param timestamp String representation of the source timestamp
     * @return A java.time.Instant representing the timestamp
     */
    private static Instant systemMetadataDateToInstant(String timestamp) {
        long nanos = Long.parseLong(timestamp) * 100;
        return Instant.EPOCH.plusNanos(nanos);
    }

    @NotNull
    public String getStreamId() {
        return streamId;
    }

    @NotNull
    public StreamRevision getStreamRevision() {
        return streamRevision;
    }

    @NotNull
    public UUID getEventId() {
        return eventId;
    }

    @NotNull
    public String getEventType() {
        return eventType;
    }

    public byte[] getEventData() {
        return eventData;
    }

    public <A> A getEventDataAs(Class<A> clazz) throws IOException {
        return mapper.readValue(this.getEventData(), clazz);
    }

    public byte[] getUserMetadata() {
        return userMetadata;
    }

    @NotNull
    public Instant getCreated() {
        return created;
    }

    @NotNull
    public Position getPosition() {
        return position;
    }

    @NotNull
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordedEvent that = (RecordedEvent) o;
        return streamId.equals(that.streamId) && streamRevision.equals(that.streamRevision) && eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, streamRevision, eventId);
    }

    static RecordedEvent fromWire(StreamsOuterClass.ReadResp.ReadEvent.RecordedEvent wireEvent) {
        UUID eventId;
        if (wireEvent.getId().hasStructured()) {
            Shared.UUID.Structured structured = wireEvent.getId().getStructured();
            eventId = new UUID(structured.getMostSignificantBits(), structured.getLeastSignificantBits());
        } else {
            eventId = UUID.fromString(wireEvent.getId().getString());
        }

        return new RecordedEvent(
                wireEvent.getStreamIdentifier().getStreamName().toStringUtf8(),
                new StreamRevision(wireEvent.getStreamRevision()),
                eventId,
                new Position(wireEvent.getCommitPosition(), wireEvent.getPreparePosition()),
                wireEvent.getMetadataMap(),
                wireEvent.getData().toByteArray(),
                wireEvent.getCustomMetadata().toByteArray());
    }

    static RecordedEvent fromWire(Persistent.ReadResp.ReadEvent.RecordedEvent wireEvent) {
        UUID eventId;
        if (wireEvent.getId().hasStructured()) {
            Shared.UUID.Structured structured = wireEvent.getId().getStructured();
            eventId = new UUID(structured.getMostSignificantBits(), structured.getLeastSignificantBits());
        } else {
            eventId = UUID.fromString(wireEvent.getId().getString());
        }

        return new RecordedEvent(
                wireEvent.getStreamIdentifier().getStreamName().toStringUtf8(),
                new StreamRevision(wireEvent.getStreamRevision()),
                eventId,
                new Position(wireEvent.getCommitPosition(), wireEvent.getPreparePosition()),
                wireEvent.getMetadataMap(),
                wireEvent.getData().toByteArray(),
                wireEvent.getCustomMetadata().toByteArray());
    }
}

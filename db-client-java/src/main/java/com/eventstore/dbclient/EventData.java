package com.eventstore.dbclient;

import java.util.UUID;

public final class EventData {
    private final UUID eventId;
    private final String eventType;
    private final String contentType;
    private final byte[] eventData;
    private final byte[] userMetadata;

    public EventData(UUID eventId, String eventType, String contentType, byte[] eventData, byte[] userMetadata) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.contentType = contentType;
        this.eventData = eventData;
        this.userMetadata = userMetadata;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getEventData() {
        return eventData;
    }

    public byte[] getUserMetadata() {
        return userMetadata;
    }

    public static <A> EventDataBuilder builderAsJson(String eventType, A payload) {
        return EventDataBuilder.json(eventType, payload);
    }

    public static EventDataBuilder builderAsBinary(String eventType, byte[] payload) {
        return EventDataBuilder.binary(eventType, payload);
    }
}


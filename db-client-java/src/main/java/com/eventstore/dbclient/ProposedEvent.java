package com.eventstore.dbclient;

import java.util.UUID;

public final class ProposedEvent {
    private final UUID eventId;
    private final String eventType;
    private final String contentType;
    private final byte[] eventData;
    private final byte[] userMetadata;

    public ProposedEvent(UUID eventId, String eventType, String contentType, byte[] eventData, byte[] userMetadata) {
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
}


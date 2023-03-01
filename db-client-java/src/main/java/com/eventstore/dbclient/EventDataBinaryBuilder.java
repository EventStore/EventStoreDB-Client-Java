package com.eventstore.dbclient;

import java.util.UUID;

/**
 * Utility class to help building an <i>EventData</i>.
 */
public class EventDataBinaryBuilder {
    private byte[] eventData;
    private byte[] metadata;
    private String eventType;
    private boolean isJson;
    private UUID id;

    EventDataBinaryBuilder(){}

    /**
     * Configures an event data builder to host a binary payload.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBinaryBuilder binary(String eventType, byte[] eventData) {
        return binary(null, eventType, eventData);
    }

    /**
     * Configures an event data builder to host a binary payload.
     * @param id event's id.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBinaryBuilder binary(UUID id, String eventType, byte[] eventData) {
        EventDataBinaryBuilder self = new EventDataBinaryBuilder();

        self.eventData = eventData;
        self.eventType = eventType;
        self.isJson = false;
        self.id = id;

        return self;
    }

    /**
     * Sets event's unique identifier.
     */
    public EventDataBinaryBuilder eventId(UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Sets event's custom user metadata.
     */
    public EventDataBinaryBuilder metadataAsBytes(byte[] value) {
        this.metadata = value;
        return this;
    }

    /**
     * Builds an event ready to be sent to EventStoreDB.
     * @see EventData
     */
    public EventData build() {
        UUID eventId = this.id == null ? UUID.randomUUID() : this.id;
        String contentType = this.isJson ? "application/json" : "application/octet-stream";
        return new EventData(eventId, this.eventType, contentType, this.eventData, this.metadata);
    }
}
package com.eventstore.dbclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.UUID;

public class EventDataBuilder {
    private static final JsonMapper mapper = new JsonMapper();
    private byte[] eventData;
    private byte[] metadata;
    private String eventType;
    private boolean isJson;
    private UUID id;

    public static <A> EventDataBuilder json(String eventType, A eventData) {
        return json(null, eventType, eventData);
    }

    public static <A> EventDataBuilder json(UUID id, String eventType, A eventData) {
        try {
            return json(id, eventType, mapper.writeValueAsBytes(eventData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static EventDataBuilder json(String eventType, byte[] eventData) {
        return json(null, eventType, eventData);
    }

    public static EventDataBuilder json(UUID id, String eventType, byte[] eventData) {
        EventDataBuilder self = new EventDataBuilder();
        self.eventData = eventData;
        self.eventType = eventType;
        self.isJson = true;
        self.id = id;

        return self;
    }

    public static EventDataBuilder binary(String eventType, byte[] eventData) {
        return binary(null, eventType, eventData);
    }

    public static EventDataBuilder binary(UUID id, String eventType, byte[] eventData) {
        EventDataBuilder self = new EventDataBuilder();

        self.eventData = eventData;
        self.eventType = eventType;
        self.isJson = false;
        self.id = id;

        return self;
    }

    public EventDataBuilder eventId(UUID id) {
        this.id = id;
        return this;
    }

    public <A> EventDataBuilder metadataAsJson(A value) {
        try {
            this.metadata = mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public EventDataBuilder metadataAsBytes(byte[] value) {
        this.metadata = value;
        return this;
    }

    public EventData build() {
        UUID eventId = this.id == null ? UUID.randomUUID() : this.id;
        String contentType = this.isJson ? "application/json" : "application/octet-stream";
        return new EventData(eventId, this.eventType, contentType, this.eventData, this.metadata);
    }
}

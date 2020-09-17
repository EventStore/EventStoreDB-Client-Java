package com.eventstore.dbclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.UUID;

public class ProposedEventBuilder {
    private static final JsonMapper mapper = new JsonMapper();
    private byte[] payload;
    private byte[] metadata;
    private String eventType;
    private boolean isJson;
    private UUID id;

    public static <A> ProposedEventBuilder json(String eventType, A payload) {
        ProposedEventBuilder self = new ProposedEventBuilder();

        try {
            self.payload = mapper.writeValueAsBytes(payload);
            self.isJson = true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        self.eventType = eventType;

        return self;
    }

    public static ProposedEventBuilder binary(String eventType, byte[] payload) {
        ProposedEventBuilder self = new ProposedEventBuilder();

        self.payload = payload;
        self.eventType = eventType;
        self.isJson = false;

        return self;
    }

    public ProposedEventBuilder eventId(UUID id) {
        this.id = id;
        return this;
    }

    public <A> ProposedEventBuilder metadataAsJson(A value) {
        try {
            this.metadata = mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public ProposedEventBuilder metadataAsBytes(byte[] value) {
        this.metadata = value;
        return this;
    }

    public ProposedEvent build() {
        UUID eventId = this.id == null ? UUID.randomUUID() : this.id;
        String contentType = this.isJson ? "application/json" : "application/octet-stream";
        return new ProposedEvent(eventId, this.eventType, contentType, this.payload, this.metadata);
    }
}

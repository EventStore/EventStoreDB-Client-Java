package com.eventstore.dbclient.samples;

import com.eventstore.dbclient.Position;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.StreamRevision;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MockRecordedEvent {

  public static RecordedEventBuilder recordedEventBuilder() {
    return new RecordedEventBuilder();
  }

  public static class RecordedEventBuilder {
    private @NotNull String streamId;
    private @NotNull StreamRevision streamRevision;
    private @NotNull UUID eventId;
    private @NotNull Position position;
    private @NotNull Map<String, String> systemMetadata;
    private @NotNull byte[] eventData;
    private @NotNull byte[] userMetadata;

    RecordedEventBuilder() {
    }

    public RecordedEventBuilder streamId(@NotNull String streamId) {
      this.streamId = streamId;
      return this;
    }

    public RecordedEventBuilder streamRevision(@NotNull StreamRevision streamRevision) {
      this.streamRevision = streamRevision;
      return this;
    }
    public RecordedEventBuilder eventId(@NotNull UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    public RecordedEventBuilder position(@NotNull Position position) {
      this.position = position;
      return this;
    }

    public RecordedEventBuilder systemMetadata(@NotNull Map<String, String> systemMetadata) {
      this.systemMetadata = systemMetadata;
      return this;
    }

    public RecordedEventBuilder eventData(@NotNull byte[] eventData) {
      this.eventData = eventData;
      return this;
    }

    public RecordedEventBuilder userMetadata(@NotNull byte[] userMetadata) {
      this.userMetadata = userMetadata;
      return this;
    }

    public RecordedEvent build() {
      return new RecordedEvent(streamId, streamRevision, eventId, position, systemMetadata, eventData, userMetadata);
    }

    public String toString() {
      return "RecordedEvent.RecordedEventBuilder(streamId=" + this.streamId
              + ", streamRevision=" + this.streamRevision
              + ", eventId=" + this.eventId
              + ", position=" + this.position
              + ", systemMetadata=" + systemMetadata
              + ", eventData=" + java.util.Arrays.toString(this.eventData)
              + ", userMetadata=" + java.util.Arrays.toString(this.userMetadata) + ")";
    }
  }

  public static RecordedEventBuilder getMockRecordedEvent() {
    Map<String, String> systemMetadata = getMockSystemMetadata();

    return recordedEventBuilder()
            .streamId("")
            .streamRevision(new StreamRevision(1L))
            .eventId(UUID.randomUUID())
            .position(new Position(1L, 1L))
            .systemMetadata(systemMetadata)
            .eventData(new byte[1])
            .userMetadata(new byte[1]);
  }

  public static Map<String, String> getMockSystemMetadata() {
    Map<String, String> systemMetadata = new HashMap<>();
    systemMetadata.put("created", String.valueOf(Instant.now().toEpochMilli()));
    return systemMetadata;
  }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.samples.JacksonObjectMapperFactoryTestImpl;
import com.eventstore.dbclient.samples.MockRecordedEvent;
import com.eventstore.dbclient.samples.TestEventWithDate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerializationTests {

  private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Test
  void useCustomSerializerToSupportEventsSerializedWithJava8Features() throws IOException {
    TestEventWithDate source = new TestEventWithDate(Instant.now());
    byte[] bytes = mapper.writeValueAsBytes(source);

    RecordedEvent recordedEvent = MockRecordedEvent.getMockRecordedEvent().eventData(bytes).build();

    TestEventWithDate eventDeserialized = recordedEvent.getEventDataAs(TestEventWithDate.class);
    assertEquals(source, eventDeserialized);
  }

  @Test
  void getExceptionWithMapperRawWhenEventsSerializedWithJava8Features() throws IOException {
    System.setProperty(JacksonObjectMapperFactory.NAME, JacksonObjectMapperFactoryTestImpl.class.getName());

    TestEventWithDate source = new TestEventWithDate(Instant.now());
    byte[] bytes = mapper.writeValueAsBytes(source);
    RecordedEvent recordedEvent = new RecordedEventWrapper(MockRecordedEvent.getMockRecordedEvent().eventData(bytes).build(), MockRecordedEvent.getMockSystemMetadata());

    assertThrows(InvalidDefinitionException.class,
            () -> recordedEvent.getEventDataAs(TestEventWithDate.class),
            "Java 8 date/time type `java.time.Instant` not supported by default: add Module \"com.fasterxml.jackson.datatype:jackson-datatype-jsr310\" to enable handling\n" +
                    " at [Source: (byte[])\"{\"instant\":1651703262.160669000}\"; line: 1, column: 12] (through reference chain: com.eventstore.dbclient.SerializationTest$TestEventData[\"instant\"])\n" +
                    "com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type `java.time.Instant` not supported by default: add Module \"com.fasterxml.jackson.datatype:jackson-datatype-jsr310\" to enable handling\n" +
                    " at [Source: (byte[])\"{\"instant\":1651703262.160669000}\"; line: 1, column: 12] (through reference chain: com.eventstore.dbclient.SerializationTest$TestEventData[\"instant\"])"
    );
  }

  /**
   * This wrapper class is needed since the JacksonObjectMapperProvider has a map of serializers. In the first test case, the RecordEvent entry is added.
   * To create a different mapper with other configurations, we need this wrapper to generate other entry in the provider.
   */
  private static class RecordedEventWrapper extends RecordedEvent {
    public RecordedEventWrapper(RecordedEvent recordedEvent, Map<String, String> systemMetadata) {
      super(recordedEvent.getStreamId(),
              recordedEvent.getStreamRevision(),
              recordedEvent.getEventId(),
              recordedEvent.getPosition(),
              systemMetadata,
              recordedEvent.getEventData(),
              recordedEvent.getUserMetadata());
    }
  }
}

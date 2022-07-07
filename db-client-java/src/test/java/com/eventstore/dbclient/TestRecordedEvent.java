package com.eventstore.dbclient;

import org.junit.jupiter.api.Assertions;

import java.time.Instant;
import java.util.UUID;

/**
 * This class exists to support `jackson-databind` deserialization of test event
 * data from JSON for comparing against data in the known dataset. It is capable of
 * running it's own assertions against an instance of public API type `RecordedEvent`.
 */
class TestResolvedEvent {
    private TestRecordedEvent event;
    private TestRecordedEvent link;

    public void assertEquals(ResolvedEvent other) {
        if (event != null) {
            event.assertEquals(other.getEvent());
        } else {
            Assertions.assertNull(other.getEvent());
        }

        if (link != null) {
            link.assertEquals(other.getLink());
        } else {
            Assertions.assertNull(other.getLink());
        }
    }

    public TestRecordedEvent getEvent() {
        return event;
    }

    public void setEvent(TestRecordedEvent event) {
        this.event = event;
    }

    public TestRecordedEvent getLink() {
        return link;
    }

    public void setLink(TestRecordedEvent link) {
        this.link = link;
    }
}

/**
 * This class exists to support `jackson-databind` deserialization of test event
 * data from JSON for comparing against data in the known dataset. It is capable of
 * running it's own assertions against an instance of public API type `StreamRevision`.
 */
class TestStreamRevision {
    private long value;

    public void assertEquals(long other) {
        Assertions.assertEquals(value, other);
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}

class TestPosition {
    private long prepare;
    private long commit;

    public void assertEquals(Position other) {
        Assertions.assertEquals(prepare, other.getPrepareUnsigned());
        Assertions.assertEquals(commit, other.getCommitUnsigned());
    }

    public long getPrepare() {
        return prepare;
    }

    public void setPrepare(long prepare) {
        this.prepare = prepare;
    }

    public long getCommit() {
        return commit;
    }

    public void setCommit(long commit) {
        this.commit = commit;
    }
}

class TestInstant {
    private long seconds;
    private int nanos;

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public long getNanos() {
        return nanos;
    }

    public void setNanos(int nanos) {
        this.nanos = nanos;
    }

    public void assertEquals(Instant instant) {
        Assertions.assertEquals(Instant.ofEpochSecond(seconds, nanos), instant);
    }
}

/**
 * This class exists to support `jackson-databind` deserialization of test event
 * data from JSON for comparing against data in the known dataset. It is capable of
 * running it's own assertions against an instance of public API type `RecordedEvent`.
 */
class TestRecordedEvent {
    private String streamId;
    private TestStreamRevision streamRevision;
    private UUID eventId;
    private String eventType;
    private byte[] eventData;
    private byte[] userMetadata;
    private TestInstant created;
    private TestPosition position;
    private String contentType;

    public void assertEquals(RecordedEvent other) {
        Assertions.assertEquals(streamId, other.getStreamId());
        streamRevision.assertEquals(other.getRevision());
        Assertions.assertEquals(eventId, other.getEventId());
        Assertions.assertEquals(eventType, other.getEventType());
        created.assertEquals(other.getCreated());
        position.assertEquals(other.getPosition());
        Assertions.assertEquals(contentType, other.getContentType());
        Assertions.assertArrayEquals(eventData, other.getEventData());
        Assertions.assertArrayEquals(userMetadata, other.getUserMetadata());
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public TestStreamRevision getStreamRevision() {
        return streamRevision;
    }

    public void setStreamRevision(TestStreamRevision streamRevision) {
        this.streamRevision = streamRevision;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public byte[] getEventData() {
        return eventData;
    }

    public void setEventData(byte[] eventData) {
        this.eventData = eventData;
    }

    public byte[] getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(byte[] userMetadata) {
        this.userMetadata = userMetadata;
    }

    public TestInstant getCreated() {
        return created;
    }

    public void setCreated(TestInstant created) {
        this.created = created;
    }

    public TestPosition getPosition() {
        return position;
    }

    public void setPosition(TestPosition position) {
        this.position = position;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

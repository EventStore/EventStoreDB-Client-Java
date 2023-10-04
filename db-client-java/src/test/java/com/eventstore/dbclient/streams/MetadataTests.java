package com.eventstore.dbclient.streams;

import com.eventstore.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public interface MetadataTests extends ConnectionAware {
    @Test
    default void testSetStreamMetadata() throws Throwable {
        EventStoreDBClient client = getDatabase().defaultClient();

        StreamMetadata metadata = new StreamMetadata();

        metadata.setMaxAge(2);
        metadata.setCacheControl(15);
        metadata.setTruncateBefore(1);
        metadata.setMaxCount(12);

        Acl acl = Acls.newStreamAcl()
                .addReadRoles("admin")
                .addWriteRoles("admin")
                .addDeleteRoles("admin")
                .addMetaReadRoles("admin")
                .addMetaWriteRoles("admin");

        metadata.setAcl(acl);

        HashMap<String, Object> payload = new HashMap<>();

        String streamName = generateName();

        client.appendToStream(streamName, EventDataBuilder.json("foo", payload).build()).get();
        client.setStreamMetadata(streamName, metadata).get();

        StreamMetadata got = client.getStreamMetadata(streamName).get();

        Assertions.assertEquals(metadata, got);
    }

    @Test
    default void testReadNoExistingMetadata() throws Throwable {
        EventStoreDBClient client = getDatabase().defaultClient();
        String streamName = generateName();
        client.appendToStream(streamName, EventDataBuilder.json("bar", new HashMap<String, Object>()).build()).get();

        StreamMetadata got = client.getStreamMetadata(streamName).get();

        Assertions.assertEquals(new StreamMetadata(), got);
    }
}
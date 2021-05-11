package com.eventstore.dbclient;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import testcontainers.module.EventStoreTestDBContainer;

import java.util.HashMap;

public class MetadataTests {
    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(true);

    @Test
    public void testSetStreamMetadata() throws Throwable {
        EventStoreDBClient client = server.getClient();

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

        client.appendToStream("foo-stream", EventDataBuilder.json("foo", payload).build()).get();
        client.setStreamMetadata("foo-stream", metadata).get();

        StreamMetadata got = client.getStreamMetadata("foo-stream").get();

        Assert.assertEquals(metadata, got);
    }

    @Test
    public void testReadNoExistingMetadata() throws Throwable {
        EventStoreDBClient client = server.getClient();

        client.appendToStream("bar", EventDataBuilder.json("bar", new HashMap<String, Object>()).build()).get();

        StreamMetadata got = client.getStreamMetadata("bar").get();

        Assert.assertEquals(new StreamMetadata(), got);
    }
}

package com.eventstore.dbclient.misc;

import com.eventstore.dbclient.Acl;
import com.eventstore.dbclient.Acls;
import com.eventstore.dbclient.StreamMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

@SuppressWarnings("unchecked")
public class OfflineMetadataTests {
    @Test
    public void testSerializationIsoMorphism() throws Throwable {
        StreamMetadata expected = new StreamMetadata();
        HashMap<String, Object> custom = new HashMap<>();

        custom.put("foo", "bar");

        expected.setMaxAge(2);
        expected.setCacheControl(15);
        expected.setTruncateBefore(1);
        expected.setMaxCount(12);

        Acl acl = Acls.newStreamAcl()
                .addReadRoles("admin")
                .addWriteRoles("admin")
                .addDeleteRoles("admin")
                .addMetaReadRoles("admin")
                .addMetaWriteRoles("admin");

        expected.setAcl(acl);
        expected.setCustomProperties(custom);

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> source = mapper.readValue(mapper.writeValueAsString(expected.serialize()), HashMap.class);
        StreamMetadata actual = StreamMetadata.deserialize(source);


        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testNullAcl() throws Throwable {
        StreamMetadata expected = new StreamMetadata();

        expected.setMaxAge(2);
        expected.setCacheControl(15);
        expected.setTruncateBefore(1);
        expected.setMaxCount(12);

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> source = mapper.readValue(mapper.writeValueAsString(expected.serialize()), HashMap.class);
        StreamMetadata actual = StreamMetadata.deserialize(source);


        Assertions.assertEquals(expected, actual);
    }
}

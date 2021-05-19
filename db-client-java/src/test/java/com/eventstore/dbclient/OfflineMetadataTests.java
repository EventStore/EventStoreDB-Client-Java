package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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


        Assert.assertEquals(expected, actual);
    }
}

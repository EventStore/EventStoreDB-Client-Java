package com.eventstore.dbclient;

import java.util.HashMap;

public final class Acls {
    public static StreamAcl newStreamAcl() {
        return new StreamAcl();
    }

    public static Acl newUserStreamAcl() {
        return UserStreamAcl.getInstance();
    }

    public static Acl newSystemStreamAcl() {
        return SystemStreamAcl.getInstance();
    }

    public static Acl deserialize(Object source) {
        Acl acl = null;

        if (source != null) {
            if (source instanceof HashMap) {
                acl = StreamAcl.deserialize((HashMap<String, Object>) source);
            } else if (source instanceof String) {
                String str = (String) source;
                acl = UserStreamAcl.deserialize(str);
                acl = acl == null ? SystemStreamAcl.deserialize(str) : acl;
            } else {
                throw new RuntimeException("Unsupported type for ACL: " + source.getClass());
            }
        }

        return acl;
    }
}

package com.eventstore.dbclient;

import java.util.HashMap;

/**
 * Access control list (ACL) utility class.
 */
public final class Acls {

    /**
     * Returns a default stream ACL.
     * @see StreamAcl
     * @return acl
     */
    public static StreamAcl newStreamAcl() {
        return new StreamAcl();
    }

    /**
     * Returns a default user ACL.
     * @see UserStreamAcl
     * @return acl
     */
    public static Acl newUserStreamAcl() {
        return UserStreamAcl.getInstance();
    }

    /**
     * Returns a default system ACL.
     * @see SystemStreamAcl
     * @return acl
     */
    public static Acl newSystemStreamAcl() {
        return SystemStreamAcl.getInstance();
    }

    @SuppressWarnings("unchecked")
    static Acl deserialize(Object source) {
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

package com.eventstore.dbclient;

/**
 * Admin stream access control list (ACL).
 */
class SystemStreamAcl implements Acl {
    private static final String ACL_NAME = "$systemStreamAcl";
    private static final SystemStreamAcl SINGLETON = new SystemStreamAcl();

    private SystemStreamAcl() {}

    static SystemStreamAcl deserialize(String source) {
        SystemStreamAcl acl = null;

        if (source.equals(ACL_NAME))
            acl = SINGLETON;

        return acl;
    }

    static SystemStreamAcl getInstance() {
        return SINGLETON;
    }

    @Override
    public Object serialize() {
        return ACL_NAME;
    }

    @Override
    public int hashCode() {
        return ACL_NAME.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof  SystemStreamAcl);
    }
}

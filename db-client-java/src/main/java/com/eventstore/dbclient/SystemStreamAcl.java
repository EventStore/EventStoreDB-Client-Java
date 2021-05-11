package com.eventstore.dbclient;

public class SystemStreamAcl implements Acl {
    public static final String ACL_NAME = "$systemStreamAcl";
    private static final SystemStreamAcl SINGLETON = new SystemStreamAcl();

    private SystemStreamAcl() {}

    public static SystemStreamAcl deserialize(String source) {
        SystemStreamAcl acl = null;

        if (source.equals(ACL_NAME))
            acl = SINGLETON;

        return acl;
    }

    public static SystemStreamAcl getInstance() {
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

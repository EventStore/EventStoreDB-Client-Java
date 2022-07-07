package com.eventstore.dbclient;

/**
 * Default user stream access control list (ACL).
 */
class UserStreamAcl implements Acl {
    private static final String ACL_NAME = "$userStreamAcl";
    private static final UserStreamAcl SINGLETON = new UserStreamAcl();

    private UserStreamAcl() {}

    static UserStreamAcl deserialize(String source) {
        UserStreamAcl acl = null;

        if (source.equals(ACL_NAME))
            acl = SINGLETON;

        return acl;
    }

    static UserStreamAcl getInstance() {
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
        return (obj instanceof  UserStreamAcl);
    }
}

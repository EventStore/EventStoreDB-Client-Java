package com.eventstore.dbclient;

import java.time.Instant;
import java.util.List;

public class User {
    private final String loginName;

    private final String fullName;

    private final List<String> groups;

    private final Instant lastUpdated;

    private final boolean disabled;

    public User(String loginName, String fullName, List<String> groups, Instant lastUpdated, boolean disabled) {
        this.loginName = loginName;
        this.fullName = fullName;
        this.groups = groups;
        this.lastUpdated = lastUpdated;
        this.disabled = disabled;
    }

    public String loginName() {
        return loginName;
    }

    public String fullName() {
        return fullName;
    }

    public List<String> groups() {
        return groups;
    }

    public Instant lastUpdated() {
        return lastUpdated;
    }

    public boolean isDisabled() {
        return disabled;
    }
}

package com.eventstore.dbclient;

public class ConnectionSettings {
    public boolean dnsDiscover;
    public int maxDiscoverAttempts;
    public int discoveryInterval;
    public int gossipTimeout;
    public NodePreference nodePreference;
    public boolean tls;
    public boolean tlsVerifyCert;
    public boolean throwOnAppendFailure;
    public Credentials defaultCredentials;
    public Endpoint[] hosts;

    public ConnectionSettings(
            boolean dnsDiscover,
            int maxDiscoverAttempts,
            int discoveryInterval,
            int gossipTimeout,
            NodePreference nodePreference,
            boolean tls,
            boolean tlsVerifyCert,
            boolean throwOnAppendFailure,
            Credentials defaultCredentials,
            Endpoint[] hosts
    ) {
        this.dnsDiscover = dnsDiscover;
        this.maxDiscoverAttempts = maxDiscoverAttempts;
        this.discoveryInterval = discoveryInterval;
        this.gossipTimeout = gossipTimeout;
        this.nodePreference = nodePreference;
        this.tls = tls;
        this.tlsVerifyCert = tlsVerifyCert;
        this.throwOnAppendFailure = throwOnAppendFailure;
        this.defaultCredentials = defaultCredentials;
        this.hosts = hosts;
    }

    public static ConnectionSettingsBuilder builder() {
        return new ConnectionSettingsBuilder();
    }

    protected static class Credentials {
        public String login;
        public String password;

        public Credentials(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }
}

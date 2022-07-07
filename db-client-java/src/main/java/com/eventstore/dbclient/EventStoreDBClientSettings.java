package com.eventstore.dbclient;

/**
 * Gathers all the settings related to a gRPC client with an EventStoreDB database.
 * <i>EventStoreDBClientSettings}</i> can only be created when parsing a connection string.
 *
 * <i>EventStoreDBClientSettings</i> supports a wide range of settings. If a setting is not mentioned in the connection
 * string, that setting default value is used.
 *
 * <ul>
 *     <li>maxDiscoverAttempts: 3</li>
 *     <li>discoveryInterval: 500</li>
 *     <li>gossipTimeout: 3</li>
 *     <li>nodePreference: leader</li>
 *     <li>tls: true</li>
 *     <li>tlsVerifyCert: true</li>
 *     <li>throwOnAppendFailure: true</li>
 *     <li>keepAliveTimeout: 10000</li>
 *     <li>keepAliveInterval: 10000</li>
 * </ul>
 */
public class EventStoreDBClientSettings {
    private final boolean dnsDiscover;
    private final int maxDiscoverAttempts;
    private final int discoveryInterval;
    private final int gossipTimeout;
    private final NodePreference nodePreference;
    private final boolean tls;
    private final boolean tlsVerifyCert;
    private final boolean throwOnAppendFailure;
    private final Credentials defaultCredentials;
    private final Endpoint[] hosts;
    private final long keepAliveTimeout;
    private final long keepAliveInterval;
    private final Long defaultDeadline;

    /**
     * If the dns discovery is enabled.
     */
    public boolean isDnsDiscover() {
        return dnsDiscover;
    }

    /**
     * How many times to attempt connection before throwing.
     */
    public int getMaxDiscoverAttempts() {
        return maxDiscoverAttempts;
    }

    /**
     * How long to wait before retrying a new discovery process (in milliseconds).
     */
    public int getDiscoveryInterval() {
        return discoveryInterval;
    }

    /**
     * How long to wait for the gossip request to time out (in seconds).
     */
    public int getGossipTimeout() {
        return gossipTimeout;
    }

    /**
     * Preferred node type when picking a node within a cluster.
     * @see NodePreference
     * @return selected node preference.
     */
    public NodePreference getNodePreference() {
        return nodePreference;
    }

    /**
     * If secure mode is enabled.
     */
    public boolean isTls() {
        return tls;
    }

    /**
     * If secure mode is enabled, is certificate verification enabled.
     */
    public boolean isTlsVerifyCert() {
        return tlsVerifyCert;
    }

    /**
     * If an exception is thrown, whether an append operation fails on optimistic concurrency error.
     */
    public boolean isThrowOnAppendFailure() {
        return throwOnAppendFailure;
    }

    /**
     * Default credentials used to authenticate requests.
     * @see Credentials
     * @return default credentials null if not defined
     */
    public Credentials getDefaultCredentials() {
        return defaultCredentials;
    }

    /**
     * The list of endpoints that the client uses to connect.
     * @see Endpoint
     * @return hosts to connect to.
     */
    public Endpoint[] getHosts() {
        return hosts;
    }

    /**
     * The amount of time (in milliseconds) the sender of the keepalive ping waits for an acknowledgement.
     * If it does not receive an acknowledgement within this time, it will close the channel.
     */
    public long getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    /**
     * The amount of time (in milliseconds) to wait after which a keepalive ping is sent on the transport.
     * Use -1 to disable.
     * @return keepalive value in milliseconds.
     */
    public long getKeepAliveInterval() {
        return keepAliveInterval;
    }

    /**
     * An optional length of time (in milliseconds) to use for gRPC deadlines.
     * @return deadline value in milliseconds or null if not set.
     */
    public Long getDefaultDeadline() {
        return defaultDeadline;
    }

    EventStoreDBClientSettings(
            boolean dnsDiscover,
            int maxDiscoverAttempts,
            int discoveryInterval,
            int gossipTimeout,
            NodePreference nodePreference,
            boolean tls,
            boolean tlsVerifyCert,
            boolean throwOnAppendFailure,
            Credentials defaultCredentials,
            Endpoint[] hosts,
            long keepAliveTimeout,
            long keepAliveInterval,
            Long defaultDeadline
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
        this.keepAliveTimeout = keepAliveTimeout;
        this.keepAliveInterval = keepAliveInterval;
        this.defaultDeadline = defaultDeadline;
    }

    /**
     * Return a connection settings builder configured with default properties.
     * @see ConnectionSettingsBuilder
     * @return a builder.
     */
    public static ConnectionSettingsBuilder builder() {
        return new ConnectionSettingsBuilder();
    }

    /**
     * Holds credential information to authenticate requests.
     */
    protected static class Credentials {
        private final String login;
        private final String password;

        Credentials(String login, String password) {
            this.login = login;
            this.password = password;
        }

        UserCredentials toUserCredentials() {
            return new UserCredentials(login, password);
        }
    }
}

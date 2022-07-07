package com.eventstore.dbclient;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Utility to create client settings programmatically.
 */
public class ConnectionSettingsBuilder {
    private final Logger logger = LoggerFactory.getLogger(ConnectionSettingsBuilder.class);
    private boolean _dnsDiscover = false;
    private int _maxDiscoverAttempts = 3;
    private int _discoveryInterval = 500;
    private int _gossipTimeout = 3000;
    private NodePreference _nodePreference = NodePreference.LEADER;
    private boolean _tls = true;
    private boolean _tlsVerifyCert = true;
    private boolean _throwOnAppendFailure = true;
    private EventStoreDBClientSettings.Credentials _defaultCredentials;
    private LinkedList<Endpoint> _hosts = new LinkedList<>();
    private long _keepAliveTimeout = Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS;
    private long _keepAliveInterval = Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS;
    private Long _defaultDeadline = null;

    ConnectionSettingsBuilder() {}

    /**
     * Returns configured connection settings.
     * @see EventStoreDBClientSettings
     * @return configured settings.
     */
    public EventStoreDBClientSettings buildConnectionSettings() {
        return new EventStoreDBClientSettings(_dnsDiscover,
                _maxDiscoverAttempts,
                _discoveryInterval,
                _gossipTimeout,
                _nodePreference,
                _tls,
                _tlsVerifyCert,
                _throwOnAppendFailure,
                _defaultCredentials,
                _hosts.toArray(new Endpoint[_hosts.size()]),
                _keepAliveTimeout,
                _keepAliveInterval,
                _defaultDeadline);
    }

    /**
     * If DNS node discovery is enabled.
     */
    public ConnectionSettingsBuilder dnsDiscover(boolean dnsDiscover) {
        this._dnsDiscover = dnsDiscover;
        return this;
    }

    /**
     * How many times to attempt connection before throwing.
     */
    public ConnectionSettingsBuilder maxDiscoverAttempts(int maxDiscoverAttempts) {
        this._maxDiscoverAttempts = maxDiscoverAttempts;
        return this;
    }

    /**
     * How long to wait before retrying a new discovery process (in milliseconds).
     */
    public ConnectionSettingsBuilder discoveryInterval(int discoveryInterval) {
        this._discoveryInterval = discoveryInterval;
        return this;
    }

    /**
     * How long to wait for the gossip request to timeout (in seconds).
     */
    public ConnectionSettingsBuilder gossipTimeout(int gossipTimeout) {
        this._gossipTimeout = gossipTimeout;
        return this;
    }

    /**
     * Preferred node type when picking a node within a cluster.
     */
    public ConnectionSettingsBuilder nodePreference(NodePreference nodePreference) {
        this._nodePreference = nodePreference;
        return this;
    }

    /**
     * If secure mode is enabled.
     */
    public ConnectionSettingsBuilder tls(boolean tls) {
        this._tls = tls;
        return this;
    }

    /**
     * If secure mode is enabled, is certificate verification enabled.
     */
    public ConnectionSettingsBuilder tlsVerifyCert(boolean tlsVerifyCert) {
        this._tlsVerifyCert = tlsVerifyCert;
        return this;
    }

    /**
     * If an exception is thrown whether an append operation fails.
     */
    public ConnectionSettingsBuilder throwOnAppendFailure(boolean throwOnAppendFailure) {
        this._throwOnAppendFailure = throwOnAppendFailure;
        return this;
    }

    /**
     * Default credentials used to authenticate requests.
     */
    public ConnectionSettingsBuilder defaultCredentials(String username, String password) {
        this._defaultCredentials = new EventStoreDBClientSettings.Credentials(username, password);
        return this;
    }

    /**
     * Adds an endpoint the client will use to connect.
     * @see Endpoint
     */
    public ConnectionSettingsBuilder addHost(Endpoint host) {
        this._hosts.push(host);
        return this;
    }

    /**
     * The amount of time (in milliseconds) the sender of the keepalive ping waits for an acknowledgement.
     */
    public ConnectionSettingsBuilder keepAliveTimeout(long value) {
        if (value >= 0 && value < Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS) {
            logger.warn("Specified keepAliveTimeout of {} is less than recommended {}", value, Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS);
        } else {
            if (value == -1)
                value = Long.MAX_VALUE;

            this._keepAliveTimeout = value;
        }
        return this;
    }

    /**
     * The amount of time (in milliseconds) to wait after which a keepalive ping is sent on the transport.
     */
    public ConnectionSettingsBuilder keepAliveInterval(long value) {
        if (value >= 0 && value < Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS) {
            logger.warn("Specified keepAliveInterval of {} is less than recommended {}", value, Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS);
        } else {
            if (value == -1)
                value = Long.MAX_VALUE;

            this._keepAliveInterval = value;
        }

        return this;
    }

    /**
     * An optional length of time (in milliseconds) to use for gRPC deadlines.
     */
    public ConnectionSettingsBuilder defaultDeadline(long value) {
        this._defaultDeadline = value;
        return this;
    }
}

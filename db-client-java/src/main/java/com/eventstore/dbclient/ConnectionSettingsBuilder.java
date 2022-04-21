package com.eventstore.dbclient;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

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

    public ConnectionSettingsBuilder dnsDiscover(boolean dnsDiscover) {
        this._dnsDiscover = dnsDiscover;
        return this;
    }

    public ConnectionSettingsBuilder maxDiscoverAttempts(int maxDiscoverAttempts) {
        this._maxDiscoverAttempts = maxDiscoverAttempts;
        return this;
    }

    public ConnectionSettingsBuilder discoveryInterval(int discoveryInterval) {
        this._discoveryInterval = discoveryInterval;
        return this;
    }

    public ConnectionSettingsBuilder gossipTimeout(int gossipTimeout) {
        this._gossipTimeout = gossipTimeout;
        return this;
    }

    public ConnectionSettingsBuilder nodePreference(NodePreference nodePreference) {
        this._nodePreference = nodePreference;
        return this;
    }

    public ConnectionSettingsBuilder tls(boolean tls) {
        this._tls = tls;
        return this;
    }

    public ConnectionSettingsBuilder tlsVerifyCert(boolean tlsVerifyCert) {
        this._tlsVerifyCert = tlsVerifyCert;
        return this;
    }

    public ConnectionSettingsBuilder throwOnAppendFailure(boolean throwOnAppendFailure) {
        this._throwOnAppendFailure = throwOnAppendFailure;
        return this;
    }

    public ConnectionSettingsBuilder defaultCredentials(String username, String password) {
        this._defaultCredentials = new EventStoreDBClientSettings.Credentials(username, password);
        return this;
    }

    public ConnectionSettingsBuilder addHost(Endpoint host) {
        this._hosts.push(host);
        return this;
    }

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

    public ConnectionSettingsBuilder defaultDeadline(long value) {
        this._defaultDeadline = value;
        return this;
    }
}

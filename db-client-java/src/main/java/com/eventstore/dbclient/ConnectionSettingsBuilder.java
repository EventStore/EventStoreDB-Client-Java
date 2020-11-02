package com.eventstore.dbclient;


import java.util.LinkedList;

public class ConnectionSettingsBuilder {
    private boolean _dnsDiscover = false;
    private int _maxDiscoverAttempts = 3;
    private int _discoveryInterval = 500;
    private int _gossipTimeout = 3000;
    private NodePreference _nodePreference = NodePreference.RANDOM;
    private boolean _tls = true;
    private boolean _tlsVerifyCert = true;
    private boolean _throwOnAppendFailure = true;
    private ConnectionSettings.Credentials _defaultCredentials;
    private LinkedList<Endpoint> _hosts = new LinkedList<>();

    public ConnectionSettings buildConnectionSettings() {
        return new ConnectionSettings(_dnsDiscover,
                _maxDiscoverAttempts,
                _discoveryInterval,
                _gossipTimeout,
                _nodePreference,
                _tls,
                _tlsVerifyCert,
                _throwOnAppendFailure,
                _defaultCredentials,
                _hosts.toArray(new Endpoint[_hosts.size()])
        );
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
        this._defaultCredentials = new ConnectionSettings.Credentials(username, password);
        return this;
    }

    public ConnectionSettingsBuilder addHost(Endpoint host) {
        this._hosts.push(host);
        return this;
    }
}

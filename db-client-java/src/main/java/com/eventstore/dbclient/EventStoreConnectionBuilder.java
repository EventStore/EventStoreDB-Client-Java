package com.eventstore.dbclient;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;

public class EventStoreConnectionBuilder {
    private UserCredentials _defaultUserCredentials = null;
    private Timeouts _timeouts;
    private SslContext _sslContext = null;
    private Endpoint endpoint = null;
    private boolean requiresLeader = false;
    private boolean insecure = false;

    public EventStoreConnectionBuilder() {
        _timeouts = Timeouts.DEFAULT;
    }

    public EventStoreConnectionBuilder defaultUserCredentials(UserCredentials userCredentials) {
        _defaultUserCredentials = userCredentials;
        return this;
    }

    public EventStoreConnectionBuilder connectionTimeouts(Timeouts timeouts) {
        _timeouts = timeouts;
        return this;
    }

    public EventStoreConnectionBuilder insecure() {
        this.insecure = true;
        return this;
    }

    public EventStoreConnectionBuilder sslContext(SslContext context) {
        _sslContext = context;
        return this;
    }

    public EventStoreConnectionBuilder requiresLeader() {
        return setRequiresLeader(true);
    }

    public EventStoreConnectionBuilder setRequiresLeader(boolean value) {
        this.requiresLeader = value;
        return this;
    }

    public EventStoreConnection createSingleNodeConnection(String hostname, int port) {
        return createSingleNodeConnection(new Endpoint(hostname, port));
    }

    public EventStoreConnection createSingleNodeConnection(Endpoint endpoint) {
        return new EventStoreConnection(endpoint, null, null, _sslContext, _defaultUserCredentials, NodePreference.RANDOM, requiresLeader, insecure, _timeouts);
    }

    public EventStoreConnection createClusterConnectionUsingSeeds(Endpoint[] endpoints) {
        return createClusterConnectionUsingSeeds(endpoints, NodePreference.RANDOM);
    }

    public EventStoreConnection createClusterConnectionUsingSeeds(Endpoint[] endpoints, NodePreference nodePreference) {
        return new EventStoreConnection(null, endpoints, null, _sslContext, _defaultUserCredentials, nodePreference, requiresLeader, insecure, _timeouts);
    }

    public EventStoreConnection createClusterConnectionUsingDns(String domain) {
        return createClusterConnectionUsingDns(domain, NodePreference.RANDOM);
    }

    public EventStoreConnection createClusterConnectionUsingDns(String domain, NodePreference nodePreference) {
        return new EventStoreConnection(null, null, domain, _sslContext, _defaultUserCredentials, nodePreference, requiresLeader, insecure, _timeouts);
    }
}

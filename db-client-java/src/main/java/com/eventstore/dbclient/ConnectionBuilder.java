package com.eventstore.dbclient;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class ConnectionBuilder {
    private Timeouts _timeouts;
    private SslContext _sslContext = null;

    public ConnectionBuilder() {
        _timeouts = Timeouts.DEFAULT;
    }

    public ConnectionBuilder connectionTimeouts(Timeouts timeouts) {
        _timeouts = timeouts;
        return this;
    }

    public ConnectionBuilder sslContext(SslContext context) {
        _sslContext = context;
        return this;
    }

    public EventStoreDBConnection createSingleNodeConnection(Endpoint endpoint) {
        return new SingleNodeEventStoreDBConnection(endpoint.getHostname(), endpoint.getPort(), _timeouts, _sslContext);
    }

    public EventStoreDBConnection createSingleNodeConnection(String hostname, int port) {
        return createSingleNodeConnection(new Endpoint(hostname, port));
    }

    public EventStoreDBConnection createClusterConnectionUsingSeeds(Endpoint[] endpoints) {
        return createClusterConnectionUsingSeeds(endpoints, NodePreference.RANDOM);
    }

    public EventStoreDBConnection createClusterConnectionUsingSeeds(Endpoint[] endpoints, NodePreference nodePreference) {
        ArrayList<InetSocketAddress> addresses = new ArrayList<>();

        for (int i = 0; i < endpoints.length; ++i) {
            Endpoint seed = endpoints[i];
            InetSocketAddress address = new InetSocketAddress(seed.getHostname(), seed.getPort());

            addresses.add(address);
        }

        return new EventStoreDBClusterConnection(addresses, null, nodePreference, _timeouts, _sslContext);
    }

    public EventStoreDBConnection createClusterConnectionUsingDns(String domain) {
        return createClusterConnectionUsingDns(domain, NodePreference.RANDOM);
    }

    public EventStoreDBConnection createClusterConnectionUsingDns(String domain, NodePreference nodePreference) {
        return new EventStoreDBClusterConnection(null, domain, nodePreference, _timeouts, _sslContext);
    }
}

package com.eventstore.dbclient;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class ConnectionBuilder {
    private Timeouts _timeouts;
    private SslContext _sslContext = null;
    private long _keepAliveTimeout = Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS;
    private long _keepAliveInterval = Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS;

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

    public ConnectionBuilder keepAliveTimeout(long value) {
        if (value >= 0 && value < Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS) {
            // FIXME - Use a proper log library.
            System.out.println("Specified keepAliveTimeout of " + value + " is less than recommended " + Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS);
        } else {
            if (value == -1)
                value = Long.MAX_VALUE;

            _keepAliveTimeout = value;
        }

        return this;
    }

    public ConnectionBuilder keepAliveInterval(long value) {
        if (value >= 0 && value < Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS) {
            // FIXME - Use a proper log library.
            System.out.println("Specified keepAliveInterval of " + value + " is less than recommended " + Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS);
        } else {
            if (value == -1)
                value = Long.MAX_VALUE;

            _keepAliveInterval = value;
        }

        return this;
    }

    public GrpcClient createSingleNodeConnection(Endpoint endpoint) {
        return new SingleNodeClient(endpoint.getHostname(), endpoint.getPort(), _timeouts, _sslContext, _keepAliveTimeout, _keepAliveInterval);
    }

    public GrpcClient createSingleNodeConnection(String hostname, int port) {
        return createSingleNodeConnection(new Endpoint(hostname, port));
    }

    public GrpcClient createClusterConnectionUsingSeeds(Endpoint[] endpoints) {
        return createClusterConnectionUsingSeeds(endpoints, NodePreference.RANDOM);
    }

    public GrpcClient createClusterConnectionUsingSeeds(Endpoint[] endpoints, NodePreference nodePreference) {
        ArrayList<InetSocketAddress> addresses = new ArrayList<>();

        for (int i = 0; i < endpoints.length; ++i) {
            Endpoint seed = endpoints[i];
            InetSocketAddress address = new InetSocketAddress(seed.getHostname(), seed.getPort());

            addresses.add(address);
        }

        return new EventStoreDBClusterClient(addresses, null, nodePreference, _timeouts, _sslContext, _keepAliveTimeout, _keepAliveInterval);
    }

    public GrpcClient createClusterConnectionUsingDns(Endpoint endpoint) {
        return createClusterConnectionUsingDns(endpoint, NodePreference.RANDOM);
    }

    public GrpcClient createClusterConnectionUsingDns(Endpoint endpoint, NodePreference nodePreference) {
        return new EventStoreDBClusterClient(null, endpoint, nodePreference, _timeouts, _sslContext, Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS, Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS);
    }

    public GrpcClient createConnectionFromConnectionSettings(EventStoreDBClientSettings clientSettings) {

        ConnectionBuilder builder = new ConnectionBuilder();

        builder.keepAliveTimeout(clientSettings.getKeepAliveTimeout())
            .keepAliveInterval(clientSettings.getKeepAliveInterval());

        if (clientSettings.isTls()) {
            try {
                SslContextBuilder sslContext = GrpcSslContexts.forClient();

                if (!clientSettings.isTlsVerifyCert()) {
                    sslContext.trustManager(InsecureTrustManagerFactory.INSTANCE);
                }

                builder.sslContext(sslContext.build());
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }
        }

        if (clientSettings.isDnsDiscover()) {
            return builder.createClusterConnectionUsingDns(clientSettings.getHosts()[0], clientSettings.getNodePreference());
        }

        if (clientSettings.getHosts().length > 1) {
            return builder.createClusterConnectionUsingSeeds(
                    clientSettings.getHosts(),
                    clientSettings.getNodePreference()
            );
        }

        return builder.createSingleNodeConnection(clientSettings.getHosts()[0]);
    }
}

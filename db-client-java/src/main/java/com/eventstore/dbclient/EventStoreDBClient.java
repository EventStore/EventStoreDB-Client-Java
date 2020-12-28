package com.eventstore.dbclient;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;

public class EventStoreDBClient {
    final private GrpcClient client;
    final private UserCredentials credentials;

    private EventStoreDBClient(EventStoreDBClientSettings settings) {
        ConnectionBuilder builder = new ConnectionBuilder();
        if (settings.getDefaultCredentials() != null) {
            this.credentials = settings.getDefaultCredentials().toUserCredentials();
        } else {
            this.credentials = null;
        }

        if (settings.isTls()) {
            try {
                SslContextBuilder sslContext = GrpcSslContexts.forClient();

                if (!settings.isTlsVerifyCert()) {
                    sslContext.trustManager(InsecureTrustManagerFactory.INSTANCE);
                }

                builder.sslContext(sslContext.build());
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }
        }

        if (settings.isDnsDiscover()) {
            this.client = builder.createClusterConnectionUsingDns(settings.getHosts()[0], settings.getNodePreference());
            return;
        }

        if (settings.getHosts().length > 1) {
            this.client = builder.createClusterConnectionUsingSeeds(
                    settings.getHosts(),
                    settings.getNodePreference()
            );
            return;
        }

        this.client = builder.createSingleNodeConnection(settings.getHosts()[0]);
    }

    public static EventStoreDBClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBClient(settings);
    }

    public Streams streams() {
        return new Streams(this.client, this.credentials);
    }

    public PersistentSubscriptions persistentSubscriptions() {
        return new PersistentSubscriptions(this.client, this.credentials);
    }

    public void shutdown() throws InterruptedException {
        this.client.shutdown();
    }

    public Projections projections() {
        return new Projections(this.client, this.credentials);
    }
}

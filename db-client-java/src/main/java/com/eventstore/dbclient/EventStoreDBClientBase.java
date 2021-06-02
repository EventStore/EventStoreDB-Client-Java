package com.eventstore.dbclient;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class EventStoreDBClientBase {
    final Logger logger = LoggerFactory.getLogger(EventStoreDBClientBase.class);
    final protected GrpcClient client;
    final protected UserCredentials credentials;

    protected EventStoreDBClientBase(EventStoreDBClientSettings settings) {
        if (settings.getDefaultCredentials() != null) {
            this.credentials = settings.getDefaultCredentials().toUserCredentials();
        } else {
            this.credentials = null;
        }

        SslContext sslContext = null;

        if (settings.isTls()) {
            try {
                SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();

                if (!settings.isTlsVerifyCert()) {
                    sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                }

                sslContext = sslContextBuilder.build();
            } catch (SSLException e) {
                logger.error("Exception when creating SSL context", e);
                throw new RuntimeException(e);
            }
        }

        if (settings.isDnsDiscover()) {
            this.client = new EventStoreDBClusterClient(null, settings.getHosts()[0], settings.getNodePreference(), sslContext, settings);
            return;
        }

        if (settings.getHosts().length > 1) {
            ArrayList<InetSocketAddress> seeds = new ArrayList<>();

            for (Endpoint seed : settings.getHosts()) {
                seeds.add(new InetSocketAddress(seed.getHostname(), seed.getPort()));
            }

            this.client = new EventStoreDBClusterClient(seeds, null, settings.getNodePreference(), sslContext, settings);
            return;
        }

        this.client = new SingleNodeClient(settings.getHosts()[0].getHostname(), settings.getHosts()[0].getPort(), sslContext, settings);
    }

    public void shutdown() throws ExecutionException, InterruptedException {
        this.client.shutdown();
    }
}

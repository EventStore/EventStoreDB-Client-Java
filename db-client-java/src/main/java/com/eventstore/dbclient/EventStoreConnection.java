package com.eventstore.dbclient;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class EventStoreConnection {
    private Endpoint endpoint = null;
    private Endpoint[] gossipSeeds = null;
    private String domain;
    private SslContext sslContext = null;
    private Timeouts timeouts = null;
    private UserCredentials userCredentials = null;
    private NodePreference nodePreference;
    private boolean requiresLeader;
    private boolean insecure;

    public EventStoreConnection(Endpoint endpoint, Endpoint[] gossipSeeds, String domain, SslContext sslContext, UserCredentials userCredentials, NodePreference nodePreference, boolean requiresLeader, boolean insecure, Timeouts timeouts) {
        this.endpoint = endpoint;
        this.gossipSeeds = gossipSeeds;
        this.domain = domain;
        this.sslContext = sslContext;
        this.userCredentials = userCredentials;
        this.timeouts = timeouts;
        this.nodePreference = nodePreference;
        this.requiresLeader = requiresLeader;
        this.insecure = insecure;
    }

    public static EventStoreConnectionBuilder builder() {
        return new EventStoreConnectionBuilder();
    }

    public PersistentClient newPersistentClient() {
        return new PersistentClient(createManagedChannel(), userCredentials, requiresLeader, timeouts);
    }

    private ManagedChannel createManagedChannel() {
        List<InetSocketAddress> addresses = null;
        String target = domain != null ? domain : "";
        NettyChannelBuilder builder = null;

        if (gossipSeeds != null) {
            addresses = new ArrayList<>();

            for (int i = 0; i < gossipSeeds.length; ++i) {
                Endpoint seed = gossipSeeds[i];
                InetSocketAddress address = new InetSocketAddress(seed.getHostname(), seed.getPort());

                addresses.add(address);
            }
        }

        if (domain != null || gossipSeeds != null) {
            NameResolverRegistry
                    .getDefaultRegistry()
                    .register(new ClusterResolverFactory(addresses, nodePreference, timeouts, sslContext));

            builder = NettyChannelBuilder
                    .forTarget(target);
        } else {
            builder = NettyChannelBuilder
                    .forAddress(endpoint.getHostname(), endpoint.getPort());
        }

        if (insecure) {
            builder.usePlaintext();
        } else if (sslContext != null) {
            builder.sslContext(sslContext);
        }

        return builder
                .userAgent("Event Store Client (Java)")
                .build();
    }
}

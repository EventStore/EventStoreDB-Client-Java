package com.eventstore.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EventStoreDBClusterClient extends GrpcClient {
    private final Logger logger = LoggerFactory.getLogger(EventStoreDBClusterClient.class);

    private final List<InetSocketAddress> seedNodes;
    private final NodeSelector nodeSelector;
    private final Endpoint domainEndpoint;

    public EventStoreDBClusterClient(List<InetSocketAddress> seedNodes, Endpoint domainEndpoint, NodePreference nodePreference, SslContext sslContext, EventStoreDBClientSettings settings) {
        super(settings, sslContext);

        this.seedNodes = seedNodes;
        this.nodeSelector = new NodeSelector(nodePreference);
        this.domainEndpoint = domainEndpoint;

        startConnectionLoop();
    }

    private Tuple<Endpoint, Exception> nodeSelection() {
        List<InetSocketAddress> candidates;

        if (seedNodes != null) {
            candidates = new ArrayList<>(seedNodes);
            Collections.shuffle(candidates);
        } else {
            candidates = new ArrayList<>();
            candidates.add(new InetSocketAddress(this.domainEndpoint.getHostname(), this.domainEndpoint.getPort()));
        }

        for (InetSocketAddress seed : candidates) {
            logger.debug("Using seed node [{}] for cluster node discovery.", seed);
            try {
                Optional<ClusterInfo.Member> optionalMember = attemptDiscovery(seed).get(settings.getGossipTimeout(), TimeUnit.MILLISECONDS);
                if (optionalMember.isPresent()) {
                    ClusterInfo.Member member = optionalMember.get();
                    ClusterInfo.Endpoint endpoint = member.getHttpEndpoint();
                    Endpoint result = new Endpoint(endpoint.getAddress(), endpoint.getPort());
                    logger.debug("Selected cluster node [{}] in state [{}] for connection attempt.", result, member.getState());
                    return new Tuple<>(result, null);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Exception during the node selection process", e);
            }
        }

        return new Tuple<>(null, new NoClusterNodeFound());
    }

    private CompletableFuture<Optional<ClusterInfo.Member>> attemptDiscovery(InetSocketAddress seed) {
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(seed)
                // FIXME - Find if we could get the version out the Gradle configuration file.
                .userAgent("EventStoreDB Client (Java)");

        if (this.sslContext == null) {
            builder.usePlaintext();
        } else {
            builder.sslContext(this.sslContext);
        }

        ManagedChannel channel = builder
                .build();
        GossipClient client = new GossipClient(channel);
        return client.read()
                .whenComplete((v, e) -> client.shutdown())
                .thenApply(nodeSelector::determineBestFitNode);
    }

    @Override
    protected boolean doConnect() {
        // TODO - Discuss if we shouldn't asynchronously select a node instead.
        Tuple<Endpoint, Exception> result = nodeSelection();

        if (result.get_2() == null) {
            this.endpoint = result.get_1();
            this.channel = this.createChannel(this.endpoint);
        } else {
            logger.warn("Could not determine cluster node for connection attempt.");
            this.lastException = result.get_2();
            return false;
        }

        return true;
    }
}

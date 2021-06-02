package com.eventstore.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EventStoreDBClusterClient extends GrpcClient {
    private final Logger logger = LoggerFactory.getLogger(EventStoreDBClusterClient.class);
    private static final Set<ClusterInfo.MemberState> invalidStates;
    private static final Random random = new Random();

    static {
        invalidStates = new HashSet<ClusterInfo.MemberState>() {{
            add(ClusterInfo.MemberState.MANAGER);
            add(ClusterInfo.MemberState.SHUTTING_DOWN);
            add(ClusterInfo.MemberState.SHUT_DOWN);
            add(ClusterInfo.MemberState.UNKNOWN);
            add(ClusterInfo.MemberState.INITIALIZING);
            add(ClusterInfo.MemberState.RESIGNING_LEADER);
            add(ClusterInfo.MemberState.PRE_LEADER);
            add(ClusterInfo.MemberState.PRE_REPLICA);
            add(ClusterInfo.MemberState.PRE_READ_ONLY_REPLICA);
            add(ClusterInfo.MemberState.CLONE);
            add(ClusterInfo.MemberState.DISCOVER_LEADER);
        }};
    }

    private final List<InetSocketAddress> seedNodes;
    private final NodePreference nodePreference;
    private final Endpoint domainEndpoint;

    public EventStoreDBClusterClient(List<InetSocketAddress> seedNodes, Endpoint domainEndpoint, NodePreference nodePreference, SslContext sslContext, EventStoreDBClientSettings settings) {
        super(settings, sslContext);

        this.seedNodes = seedNodes;
        this.nodePreference = nodePreference;
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
            try {
                ClusterInfo.Endpoint endpoint = attemptDiscovery(seed).get(settings.getGossipTimeout(), TimeUnit.MILLISECONDS);

                if (endpoint != null) {
                    Endpoint result = new Endpoint(endpoint.getAddress(), endpoint.getPort());
                    return new Tuple<>(result, null);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Exception during the node selection process", e);
            }
        }

        return new Tuple<>(null, new NoClusterNodeFound());
    }

    private CompletableFuture<ClusterInfo.Endpoint> attemptDiscovery(InetSocketAddress seed) {
        ManagedChannel channel = NettyChannelBuilder.forAddress(seed)
                // FIXME - Find if we could get the version out the Gradle configuration file.
                .userAgent("EventStoreDB Client (Java)")
                .sslContext(this.sslContext)
                .build();
        GossipClient client = new GossipClient(channel);
        return client.read()
                .thenApply(this::determineBestFitNode)
                .thenApply(m -> m.map(ClusterInfo.Member::getHttpEndpoint).orElse(null));
    }

    private Optional<ClusterInfo.Member> determineBestFitNode(ClusterInfo clusterInfo) {
        return clusterInfo.getMembers()
                .stream()
                .filter(ClusterInfo.Member::isAlive)
                .filter(m -> !invalidStates.contains(m.getState()))
                .sorted((o1, o2) -> {
                    switch (nodePreference) {
                        case LEADER:
                            if (o1.getState().equals(ClusterInfo.MemberState.LEADER)) {
                                return -1;
                            }
                            if (o2.getState().equals(ClusterInfo.MemberState.LEADER)) {
                                return 1;
                            }
                            return 0;
                        case FOLLOWER:
                            if (o1.getState().equals(ClusterInfo.MemberState.FOLLOWER)) {
                                return -1;
                            }
                            if (o2.getState().equals(ClusterInfo.MemberState.FOLLOWER)) {
                                return 1;
                            }
                            return 0;
                        case READ_ONLY_REPLICA:
                            if (o1.getState().equals(ClusterInfo.MemberState.READ_ONLY_REPLICA)) {
                                return -1;
                            }
                            if (o2.getState().equals(ClusterInfo.MemberState.READ_ONLY_REPLICA)) {
                                return 1;
                            }
                            return 0;
                        case RANDOM:
                            if (random.nextBoolean()) {
                                return 1;
                            }

                            return 1;
                    }
                    return 0;
                }).findFirst();
    }

    @Override
    protected boolean doConnect() {
        // TODO - Discuss if we shouldn't asynchronously select a node instead.
        Tuple<Endpoint, Exception> result = nodeSelection();

        if (result.get_2() == null) {
            this.channel = this.createChannel(result.get_1());
        } else {
            this.lastException = result.get_2();
            return false;
        }

        return true;
    }
}

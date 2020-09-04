package com.eventstore.dbclient;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.xbill.DNS.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ClusterResolverFactory extends NameResolverProvider {
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

    private List<InetSocketAddress> seedNodes;
    private final NodePreference nodePreference;
    private final SslContext sslContext;
    private final Timeouts timeouts;

    public ClusterResolverFactory(List<InetSocketAddress> seedNodes, NodePreference nodePreference, Timeouts timeouts, SslContext sslContext) {
        this.seedNodes = seedNodes;
        this.nodePreference = nodePreference;
        this.sslContext = sslContext;
        this.timeouts = timeouts;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        return new NameResolver() {
            @Override
            public String getServiceAuthority() {
                return "eventStoreDBGossip";
            }

            @Override
            public void start(Listener2 listener) {
                List<InetSocketAddress> candidates;

                if (seedNodes != null) {
                    candidates = new ArrayList<>(seedNodes);
                    Collections.shuffle(candidates);
                } else {
                    candidates = new ArrayList<>();
                    try {
                        org.xbill.DNS.Record[] records = new Lookup(targetUri.getHost(), Type.SRV).run();
                        for (int i = 0; i < records.length; ++i) {
                            SRVRecord record = (SRVRecord) records[i];

                            candidates.add(new InetSocketAddress(record.getName().toString(true), record.getPort()));
                        }
                    } catch (TextParseException e) {
                        listener.onError(Status.INTERNAL);
                    }
                }

                for (InetSocketAddress seed : candidates) {
                    try {
                        ClusterInfo.Endpoint endpoint = attemptDiscovery(seed).get();
                        if (endpoint == null) {
                            continue;
                        }

                        InetSocketAddress addr = endpoint.toInetSocketAddress();
                        List<SocketAddress> addrs = new ArrayList<>();
                        addrs.add(addr);
                        EquivalentAddressGroup addrGroup = new EquivalentAddressGroup(addrs);
                        List<EquivalentAddressGroup> addrGroups = new ArrayList<>();
                        addrGroups.add(addrGroup);

                        listener.onResult(ResolutionResult.newBuilder()
                                .setAddresses(addrGroups)
                                .setAttributes(Attributes.EMPTY)
                                .build());
                        return;
                    } catch (InterruptedException | ExecutionException e) {
                        listener.onError(Status.INTERNAL);
                        return;
                    }
                }
            }

            @Override
            public void shutdown() {
            }
        };
    }

    @Override
    public String getDefaultScheme() {
        return seedNodes != null ? "cluster_seeds" : "cluster_dns";
    }

    private CompletableFuture<ClusterInfo.Endpoint> attemptDiscovery(InetSocketAddress seed) {
        ManagedChannel channel = NettyChannelBuilder.forAddress(seed)
                .userAgent("Event Store Client (Java) v1.0.0-SNAPSHOT")
                .sslContext(this.sslContext)
                .build();
        GossipClient client = new GossipClient(channel, timeouts);
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
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 6; // We made sure to have an higher priority than the DNS resolver factory.
    }
}
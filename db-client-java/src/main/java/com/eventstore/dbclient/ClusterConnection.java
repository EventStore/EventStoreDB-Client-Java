package com.eventstore.dbclient;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class ClusterConnection implements EventStoreNodeConnection {
    private static final Set<ClusterInfo.MemberState> invalidStates;
    private static final Random random = new Random();
    private volatile boolean shutdown = false;
    private boolean doDraining = true;
    private UUID currentChannelId;
    private ManagedChannel channel;
    private Exception lastException;

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
    private final String domainName;
    private final SslContext sslContext;
    private final Timeouts timeouts;
    private LinkedBlockingQueue<Msg> messages;

    public ClusterConnection(List<InetSocketAddress> seedNodes, String domainName, NodePreference nodePreference, Timeouts timeouts, SslContext sslContext) {
        this.seedNodes = seedNodes;
        this.nodePreference = nodePreference;
        this.sslContext = sslContext;
        this.timeouts = timeouts;
        this.domainName = domainName;
        this.currentChannelId = UUID.randomUUID();

        try {
            this.messages.put(new CreateChannel(this.currentChannelId));
        } catch (InterruptedException e){
            // Impossible situation.
        }
        CompletableFuture.runAsync(() -> this.messageLoop());
    }

    private ManagedChannel createChannel(Endpoint endpoint) {
        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(endpoint.getHostname(), endpoint.getPort());

        if (this.sslContext == null) {
            builder.usePlaintext();
        } else {
            builder.sslContext(this.sslContext);
        }

        return builder.build();
    }

    private void messageLoop() {
        for(;;) {
            try {
                Msg msg = this.messages.take();

                if (!msg.accept(this)) {
                    this.shutdown = true;
                    break;
                }
            } catch (InterruptedException e) {
                this.lastException = e;
                this.shutdown = true;
                this.doDraining = false;
                break;
            }
        }

        if (this.doDraining) {
            ArrayList<Msg> msgs = new ArrayList<>();
            this.messages.drainTo(msgs);

            for (Msg msg: msgs) {
                msg.accept(this);
            }
        }
    }

    private Tuple<Endpoint, Exception> nodeSelection() {
        List<InetSocketAddress> candidates;

        if (seedNodes != null) {
            candidates = new ArrayList<>(seedNodes);
            Collections.shuffle(candidates);
        } else {
            candidates = new ArrayList<>();
            try {
                org.xbill.DNS.Record[] records = new Lookup(this.domainName, Type.SRV).run();
                for (int i = 0; i < records.length; ++i) {
                    SRVRecord record = (SRVRecord) records[i];

                    candidates.add(new InetSocketAddress(record.getName().toString(true), record.getPort()));
                }
            } catch (TextParseException e) {
                return new Tuple<>(null, e);
            }
        }

        for (InetSocketAddress seed : candidates) {
            try {
                ClusterInfo.Endpoint endpoint = attemptDiscovery(seed).get();
                if (endpoint == null) {
                    continue;
                }

                Endpoint result = new Endpoint(endpoint.getAddress(), endpoint.getPort());
                return new Tuple<>(result, null);
            } catch (InterruptedException | ExecutionException e) {
                return new Tuple<>(null, e);
            }
        }

        return new Tuple<>(null, new NoClusterNodeFound());
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
    public <A> CompletableFuture<A> run(Function<ManagedChannel, CompletableFuture<A>> action) {
        final CompletableFuture<A> result = new CompletableFuture<>();
        final ClusterConnection self = this;

        this.messages.add(new RunWorkItem(new WorkItem() {
            @Override
            public void execute(UUID id, ManagedChannel channel, Exception fatalError) {
                if (fatalError != null) {
                    result.completeExceptionally(fatalError);
                    return;
                }

                action.apply(channel).whenComplete((outcome, error) -> {
                    if (outcome != null) {
                        result.complete(outcome);
                        return;
                    }

                    if (error instanceof NotLeaderException) {
                        NotLeaderException ex = (NotLeaderException) error;
                        // TODO - Currently we don't retry on not leader exception but we might consider
                        // allowing this on a case-by-case basis.
                        result.completeExceptionally(ex);
                        try {
                            self.messages.put(new CreateChannel(id, ex.getLeaderEndpoint()));
                        } catch (InterruptedException e) {
                            // TODO - Logging but technically impossible situation.
                        }
                        return;
                    }

                    if (error instanceof StatusRuntimeException) {
                        StatusRuntimeException ex = (StatusRuntimeException) error;

                        if (ex.getStatus().getCode().equals(Status.Code.UNAVAILABLE)) {
                            self.messages.add(new CreateChannel(id));
                        }
                    }

                    result.completeExceptionally(error);
                });
            }
        }));

        return result;
    }

    private boolean createNewChannel(UUID previousId, Optional<Endpoint> candidate) {
        if (this.currentChannelId.equals(previousId)) {
            this.currentChannelId = UUID.randomUUID();

            if (candidate.isPresent()) {
                this.channel = this.createChannel(candidate.get());
            } else {
                // TODO - Discuss if we shouldn't asynchronously select a node instead.
                Tuple<Endpoint, Exception> result = nodeSelection();

                if (result.get_2() == null) {
                    this.channel = this.createChannel(result.get_1());
                } else {
                    this.lastException = result.get_2();
                    return false;
                }
            }
        }

        return true;
    }

    private boolean runWorkItem(WorkItem item) {
        if (this.shutdown) {
            Exception e = this.lastException != null ? this.lastException : new ConnectionShutdownException();
            item.execute(null, null, e);
        } else {
            // In case if the channel hasn't been resolved yet.
            if (this.channel == null) {
                try {
                    this.messages.put(new RunWorkItem(item));
                } catch (InterruptedException e) {
                    item.execute(null, null, e);
                }
            } else {
                item.execute(this.currentChannelId, this.channel, null);
            }
        }

        return true;
    }

    @Override
    public void shutdown() throws InterruptedException {
        sendMessage(new Shutdown());
    }

    private void sendMessage(Msg msg) throws InterruptedException {
        if (!this.shutdown) {
            this.messages.add(msg);
        } else if (msg instanceof RunWorkItem) {
            RunWorkItem workItem = (RunWorkItem) msg;

            workItem.reportError(new ConnectionShutdownException());
        }
    }

    interface WorkItem {
        void execute(UUID id, ManagedChannel channel, Exception error);
    }

    interface Msg {
        boolean accept(ClusterConnection self);
    }

    class CreateChannel implements Msg {
        final Optional<Endpoint> channel;
        final UUID previousId;

        CreateChannel(UUID previousId) {
            this.channel = Optional.empty();
            this.previousId = previousId;
        }

        CreateChannel(UUID previousId, Endpoint endpoint) {
            this.channel = Optional.of(endpoint);
            this.previousId = previousId;
        }

        @Override
        public boolean accept(ClusterConnection self) {
            return self.createNewChannel(previousId, channel);
        }
    }

    class RunWorkItem implements Msg {
        final WorkItem item;

        RunWorkItem(WorkItem item) {
            this.item = item;
        }

        @Override
        public boolean accept(ClusterConnection self) {
            return self.runWorkItem(item);
        }

        void reportError(Exception e) {
            this.item.execute(null, null, e);
        }
    }

    class Shutdown implements Msg {
        @Override
        public boolean accept(ClusterConnection self) {
            return false;
        }
    }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.gossip.GossipGrpc;
import com.eventstore.dbclient.proto.gossip.GossipOuterClass;
import com.eventstore.dbclient.proto.shared.Shared;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;

public class GossipClient {
    private final ManagedChannel channel;
    private final GossipGrpc.GossipStub stub;
    private final Timeouts timeouts;

    public GossipClient(ManagedChannel channel, Timeouts timeouts) {
        this.channel = channel;
        this.timeouts = timeouts;

        this.stub = GossipGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        this.channel.shutdown().awaitTermination(timeouts.shutdownTimeout, timeouts.shutdownTimeoutUnit);
    }

    public CompletableFuture<ClusterInfo> readGossip() {
        CompletableFuture<ClusterInfo> future = new CompletableFuture<>();

        this.stub.read(Shared.Empty.getDefaultInstance(), new StreamObserver<GossipOuterClass.ClusterInfo>() {
            @Override
            public void onNext(GossipOuterClass.ClusterInfo value) {
                ClusterInfo info = ClusterInfo.fromWire(value);
                future.complete(info);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        });

        return future;
    }
}

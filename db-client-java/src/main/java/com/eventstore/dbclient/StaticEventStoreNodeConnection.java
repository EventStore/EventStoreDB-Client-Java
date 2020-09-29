package com.eventstore.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class StaticEventStoreNodeConnection implements EventStoreNodeConnection {
    private String host;
    private int port;
    private ManagedChannel channel;
    private SslContext context;

    public StaticEventStoreNodeConnection(String host, int port, SslContext context) {
        this.host = host;
        this.port = port;
        this.context = context;
        this.channel = createChannel();
    }

    private ManagedChannel createChannel() {
        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(this.host, this.port);

        if (this.context == null) {
            builder.usePlaintext();
        } else {
            builder.sslContext(context);
        }

        return builder.build();
    }

    @Override
    public <A> CompletableFuture<A> run(Function<ManagedChannel, CompletableFuture<A>> action) {
        return action.apply(this.channel);
    }

    @Override
    public void shutdown() throws InterruptedException {
        this.channel.shutdown().awaitTermination(Timeouts.DEFAULT.shutdownTimeout, Timeouts.DEFAULT.shutdownTimeoutUnit);
    }
}

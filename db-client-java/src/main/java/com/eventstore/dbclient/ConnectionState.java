package com.eventstore.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

class ConnectionState {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionState.class);
    private final static int MAX_INBOUND_MESSAGE_LENGTH = 17 * 1_024 * 1_024; // 17MiB
    private final EventStoreDBClientSettings settings;
    private final SslContext sslContext;
    private InetSocketAddress previous;
    private ManagedChannel currentChannel;

    ConnectionState(EventStoreDBClientSettings settings) {
        this.settings = settings;

        if (settings.isTls()) {
            try {
                SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();

                if (!settings.isTlsVerifyCert()) {
                    sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                }

                this.sslContext = sslContextBuilder.build();
            } catch (SSLException e) {
                logger.error("Exception when creating SSL context", e);
                throw new RuntimeException(e);
            }
        } else {
            this.sslContext = null;
        }
    }

    InetSocketAddress getLastConnectedEndpoint() {
        return this.previous;
    }

    ManagedChannel getCurrentChannel() {
        return this.currentChannel;
    }

    EventStoreDBClientSettings getSettings() {
        return this.settings;
    }

    void connect(InetSocketAddress addr) {
        this.closeChannel();

        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(addr)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_LENGTH);

        if (this.sslContext == null) {
            builder.usePlaintext();
        } else {
            builder.sslContext(this.sslContext);
        }

        if (settings.getKeepAliveTimeout() <= 0)
            builder.keepAliveTimeout(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        else
            builder.keepAliveTimeout(settings.getKeepAliveTimeout(), TimeUnit.MILLISECONDS);

        if (settings.getKeepAliveInterval() <= 0)
            builder.keepAliveTime(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        else
            builder.keepAliveTime(settings.getKeepAliveInterval(), TimeUnit.MILLISECONDS);

        this.currentChannel = builder.build();
        this.previous = addr;
    }

    private void closeChannel() {
        if (this.currentChannel != null) {
            try {
                logger.trace("Shutting down existing gRPC channel [{}]", this.currentChannel);
                boolean terminated = this.currentChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                if (!terminated) {
                    this.currentChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
                }
                logger.trace("Successful shutdown of gRPC channel [{}]", this.currentChannel);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                this.currentChannel = null;
            }
        }
    }

    public void shutdown() {
        this.closeChannel();
    }

    public void clear() {
        this.previous = null;
    }
}

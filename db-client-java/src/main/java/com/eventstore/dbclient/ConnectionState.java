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
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

class ConnectionState {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionState.class);
    private final static int MAX_INBOUND_MESSAGE_LENGTH = 17 * 1_024 * 1_024; // 17MiB
    private final EventStoreDBClientSettings settings;
    private InetSocketAddress endpoint;
    private ManagedChannel currentChannel;
    private UserCertificate userCertificate;
    private SslContext sslContext;

    // Indicates if the current channel passed all the connection pre-requisites to be used by the user
    // Not exhaustive list includes:
    // * If we managed to get a gossip seed from the channel
    // * If we managed to read the server features (if not, it was a not found error then it's not fatal, just old node version)
    private boolean confirmedChannel;

    ConnectionState(EventStoreDBClientSettings settings) {
        this.settings = settings;

        buildSslContext(settings.getDefaultUserCertificate());
    }

    ManagedChannel getCurrentChannel() {
        return this.currentChannel;
    }

    EventStoreDBClientSettings getSettings() {
        return this.settings;
    }

    InetSocketAddress getLastConnectedEndpoint() {
        return this.confirmedChannel ? this.endpoint : null;
    }

    boolean shouldRecreateSslContext(AuthOptionsBase authOptions) {
        return !Objects.equals(
                getUserCertificateOrDefault(authOptions),
                this.userCertificate);
    }

    void confirmChannel() {
        this.confirmedChannel = true;
    }

    void connect(InetSocketAddress endpoint, AuthOptionsBase authOptions) {
        this.closeChannel();

        NettyChannelBuilder channelBuilder = NettyChannelBuilder
                .forAddress(endpoint)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_LENGTH)
                .intercept(this.settings.getInterceptors());

        if (shouldRecreateSslContext(authOptions))
            buildSslContext(getUserCertificateOrDefault(authOptions));

        if (this.sslContext != null)
            channelBuilder.sslContext(this.sslContext);
        else
            channelBuilder.usePlaintext();

        if (this.settings.getKeepAliveTimeout() <= 0)
            channelBuilder.keepAliveTimeout(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        else
            channelBuilder.keepAliveTimeout(this.settings.getKeepAliveTimeout(), TimeUnit.MILLISECONDS);

        if (this.settings.getKeepAliveInterval() <= 0)
            channelBuilder.keepAliveTime(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        else
            channelBuilder.keepAliveTime(this.settings.getKeepAliveInterval(), TimeUnit.MILLISECONDS);

        this.currentChannel = channelBuilder.build();
        this.confirmedChannel = false;
        this.endpoint = endpoint;
    }

    private UserCertificate getUserCertificateOrDefault(AuthOptionsBase authOptions) {
        UserCertificate defaultUserCertificate = this.settings.getDefaultUserCertificate();

        if (authOptions == null || authOptions.getUserCertificate() == null)
            return defaultUserCertificate;

        return authOptions.getUserCertificate();
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

    private void buildSslContext(UserCertificate userCertificate) {
        if (!settings.isTls()) return;

        SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();

        if (userCertificate != null) {
            sslContextBuilder.keyManager(
                    new File(userCertificate.getCertFile()),
                    new File(userCertificate.getKeyFile()));

            this.userCertificate = userCertificate;
        }

        if (this.settings.getTlsCaFile() != null)
            sslContextBuilder.trustManager(new File(this.settings.getTlsCaFile()));

        if (!this.settings.isTlsVerifyCert())
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);

        try {
            this.sslContext = sslContextBuilder.build();
        } catch (SSLException e) {
            logger.error("Exception when creating SSL context", e);
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        this.closeChannel();
    }

    public void clear() {
        this.endpoint = null;
        this.confirmedChannel = false;
    }
}

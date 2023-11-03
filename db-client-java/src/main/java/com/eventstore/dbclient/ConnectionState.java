package com.eventstore.dbclient;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

class ConnectionState {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionState.class);
    private final static int MAX_INBOUND_MESSAGE_LENGTH = 17 * 1_024 * 1_024; // 17MiB
    private final EventStoreDBClientSettings settings;
    private final ChannelCredentials channelCredentials;
    private InetSocketAddress previous;
    private ManagedChannel currentChannel;

    ConnectionState(EventStoreDBClientSettings settings) {
        this.settings = settings;
        if (settings.isTls()) {
            try {
                TlsChannelCredentials.Builder builder = TlsChannelCredentials.newBuilder();

                if (settings.getTlsCaFile() != null)
                    builder.trustManager(new File(settings.getTlsCaFile()));

                if (!settings.isTlsVerifyCert()) {
                    builder.trustManager(new InsecureTrustManager());
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, new TrustManager[] {new InsecureTrustManager()}, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                }

                this.channelCredentials = builder.build();
            } catch (Exception e) {
                logger.error("Exception when creating SSL context", e);
                throw new RuntimeException(e);
            }
        } else {
            this.channelCredentials = InsecureChannelCredentials.create();
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

        long keepAliveTimeoutInMs = settings.getKeepAliveTimeout() <= 0 ? Long.MAX_VALUE : settings.getKeepAliveTimeout();
        long keepAliveIntervalInMs = settings.getKeepAliveInterval() <= 0 ? Long.MAX_VALUE : settings.getKeepAliveInterval();
        this.currentChannel = Grpc.newChannelBuilderForAddress(addr.getHostName(), addr.getPort(), this.channelCredentials)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_LENGTH)
                .intercept(settings.getInterceptors())
                .keepAliveTime(keepAliveIntervalInMs, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(keepAliveTimeoutInMs, TimeUnit.MILLISECONDS)
                .build();

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

    public static class InsecureTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            // No need to implement
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            // Trust all certificates
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0]; // Accept any issuer
        }
    }
}

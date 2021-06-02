package com.eventstore.dbclient;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;

public class SingleNodeClient extends GrpcClient {
    private final String host;
    private final int port;

    public SingleNodeClient(String host, int port, SslContext context, EventStoreDBClientSettings settings) {
        super(settings, context);

        this.host = host;
        this.port = port;

        startConnectionLoop();
    }

    @Override
    protected boolean doConnect() {
        this.channel = createChannel(new Endpoint(host, port));

        return true;
    }
}

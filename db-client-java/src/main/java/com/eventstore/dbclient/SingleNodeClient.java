package com.eventstore.dbclient;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;

class SingleNodeClient extends GrpcClient {
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
        this.endpoint = new Endpoint(host, port);
        this.channel = createChannel(this.endpoint);

        return true;
    }
}

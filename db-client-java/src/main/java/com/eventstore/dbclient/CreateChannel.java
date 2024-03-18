package com.eventstore.dbclient;

import java.net.InetSocketAddress;
import java.util.StringJoiner;
import java.util.UUID;

class CreateChannel implements Msg {
    final UUID previousId;
    final InetSocketAddress endpoint;
    final AuthOptionsBase authOptions;

    public CreateChannel(UUID previousId, AuthOptionsBase authOptions) {
        this.endpoint = null;
        this.previousId = previousId;
        this.authOptions = authOptions;
    }

    public CreateChannel(UUID previousId, InetSocketAddress endpoint, AuthOptionsBase authOptions) {
        this.endpoint = endpoint;
        this.previousId = previousId;
        this.authOptions = authOptions;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CreateChannel.class.getSimpleName() + "[", "]")
                .add("endpoint=" + (endpoint != null ? endpoint.toString() : "NOT_SET"))
                .toString();
    }

    @Override
    public void accept(ConnectionService handler) {
        handler.createChannel(this.previousId, this.endpoint, this.authOptions);
    }
}

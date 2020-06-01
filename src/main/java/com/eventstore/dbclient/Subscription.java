package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import io.grpc.stub.ClientCallStreamObserver;

public class Subscription {
    private final ClientCallStreamObserver<StreamsOuterClass.ReadReq> _requestStream;
    private final String _subscriptionId;
    private final Checkpointer _checkpointer;

    Subscription(ClientCallStreamObserver<StreamsOuterClass.ReadReq> requestStream, String subscriptionId, Checkpointer checkpointer) {
        this._requestStream = requestStream;
        this._subscriptionId = subscriptionId;
        this._checkpointer = checkpointer;
    }

    public String getSubscriptionId() {
        return _subscriptionId;
    }

    public void stop() {
        this._requestStream.cancel("user-initiated", null);
    }

    Checkpointer getCheckpointer() {
        return this._checkpointer;
    }
}

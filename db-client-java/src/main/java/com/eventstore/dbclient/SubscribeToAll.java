package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

class SubscribeToAll extends AbstractRegularSubscription {
    private final SubscribeToAllOptions options;

    public SubscribeToAll(GrpcClient client, SubscriptionListener listener, SubscribeToAllOptions options) {
        super(client, options);

        this.options = options;
        this.listener = listener;
    }

    @Override
    protected StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        StreamsOuterClass.ReadReq.Options.Builder options =
                defaultSubscribeOptions.clone()
                        .setResolveLinks(this.options.shouldResolveLinkTos())
                        .setAll(StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder()
                                .setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                                        .setCommitPosition(this.options.getPosition().getCommitUnsigned())
                                        .setPreparePosition(this.options.getPosition().getPrepareUnsigned()))
                                .build());

        if (this.options.getFilter() != null) {
            this.options.getFilter().addToWireStreamsReadReq(options);
            this.checkpointer = this.options.getFilter().getCheckpointer();
        } else {
            options.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        return options;
    }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

public class SubscribeToAll extends AbstractRegularSubscription {
    private final SubscribeToAllOptions options;

    public SubscribeToAll(GrpcClient client, SubscriptionListener listener, SubscribeToAllOptions options) {
        super(client, options.getMetadata());

        this.options = options;
        this.listener = listener;
    }

    @Override
    protected StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        SubscriptionFilter filter = this.options.getFilter();
        StreamsOuterClass.ReadReq.Options.Builder options =
                defaultSubscribeOptions.clone()
                        .setResolveLinks(this.options.shouldResolveLinkTos())
                        .setAll(StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder()
                                .setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                                        .setCommitPosition(this.options.getPosition().getCommitUnsigned())
                                        .setPreparePosition(this.options.getPosition().getPrepareUnsigned()))
                                .build());

        if (filter != null) {
            Shared.FilterOptions.Builder filterOptionsBuilder = filter.getBuilder();
            this.checkpointer = this.options.getFilter().getCheckpointer();

            if (filterOptionsBuilder == null) {
                options.setNoFilter(Shared.Empty.getDefaultInstance());
            } else {
                options.setFilter(filterOptionsBuilder);
            }
        } else {
            options.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        return options;
    }
}

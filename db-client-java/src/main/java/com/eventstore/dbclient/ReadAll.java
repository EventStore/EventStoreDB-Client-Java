package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

public class ReadAll extends AbstractRead {
    private final ReadAllOptions options;
    private final long maxCount;

    public ReadAll(GrpcClient client, long maxCount, ReadAllOptions options) {
        super(client, options.getMetadata());

        this.maxCount = maxCount;
        this.options = options;
    }

    @Override
    public StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        return defaultReadOptions.clone()
                .setAll(StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder()
                        .setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                                .setCommitPosition(this.options.getPosition().getCommitUnsigned())
                                .setPreparePosition(this.options.getPosition().getPrepareUnsigned())))
                .setResolveLinks(this.options.shouldResolveLinkTos())
                .setCount(this.maxCount)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(this.options.getDirection() == Direction.Forward ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);
    }
}

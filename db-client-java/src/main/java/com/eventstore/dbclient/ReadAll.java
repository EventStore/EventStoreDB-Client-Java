package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

public class ReadAll extends AbstractRead {
    private final ReadAllOptions options;

    public ReadAll(GrpcClient client, ReadAllOptions options) {
        super(client, options.getMetadata());

        this.options = options;
    }

    @Override
    public StreamsOuterClass.ReadReq.Options.Builder createOptions(long count) {
        return defaultReadOptions.clone()
                .setAll(StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder()
                        .setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                                .setCommitPosition(this.options.getPosition().getCommitUnsigned())
                                .setPreparePosition(this.options.getPosition().getPrepareUnsigned())))
                .setResolveLinks(this.options.getResolveLinks())
                .setCount(count)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(this.options.getDirection() == Direction.Forward ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);
    }
}

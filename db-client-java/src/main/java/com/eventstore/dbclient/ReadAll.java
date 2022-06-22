package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

class ReadAll extends AbstractRead {
    private final ReadAllOptions options;
    private final long maxCount;

    public ReadAll(GrpcClient client, long maxCount, ReadAllOptions options) {
        super(client, options);

        this.maxCount = maxCount;
        this.options = options;
    }

    @Override
    public StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        StreamsOuterClass.ReadReq.Options.AllOptions.Builder optionsOrBuilder =
                StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder();

        if (this.options.getPosition().equals(Position.END)) {
               optionsOrBuilder.setEnd(Shared.Empty.getDefaultInstance());
        } else if (this.options.getPosition().equals(Position.START)) {
               optionsOrBuilder.setStart(Shared.Empty.getDefaultInstance());
        } else {
               optionsOrBuilder.setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                       .setCommitPosition(this.options.getPosition().getCommitUnsigned())
                       .setPreparePosition(this.options.getPosition().getPrepareUnsigned()));
        }

        StreamsOuterClass.ReadReq.Options.Builder builder = defaultReadOptions.clone()
                .setAll(optionsOrBuilder)
                .setResolveLinks(this.options.shouldResolveLinkTos())
                .setControlOption(StreamsOuterClass.ReadReq.Options.ControlOption.newBuilder().setCompatibility(1))
                .setCount(this.maxCount)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(this.options.getDirection() == Direction.Forwards ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);

        return builder;
    }
}

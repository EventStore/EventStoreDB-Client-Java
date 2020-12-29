package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

/**
 * TODO - Implement a better way to handle very long stream to not end
 * if OOM
 */
public class ReadStream extends AbstractRead {
    private final String streamName;
    private final ReadStreamOptions options;
    private final long maxCount;

    public ReadStream(GrpcClient client, String streamName, long maxCount, ReadStreamOptions options) {
        super(client, options.getMetadata());

        this.streamName = streamName;
        this.maxCount = maxCount;
        this.options = options;
    }

    @Override
    public StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        return defaultReadOptions.clone()
                .setStream(GrpcUtils.toStreamOptions(this.streamName, this.options.getStartingRevision()))
                .setResolveLinks(this.options.getResolveLinks())
                .setCount(this.maxCount)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(this.options.getDirection() == Direction.Forward ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);
    }
}

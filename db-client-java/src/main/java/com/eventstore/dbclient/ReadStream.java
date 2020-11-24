package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

/**
 * TODO - Implement a better way to handle very long stream to not end
 * if OOM
 */
public class ReadStream extends AbstractRead {
    private String streamName;
    private StreamRevision startRevision;
    private Timeouts timeouts;
    private boolean resolveLinks;
    private Direction direction;

    public ReadStream(GrpcClient client, String streamName, UserCredentials credentials) {
        super(client);

        this.streamName = streamName;
        this.metadata = new ConnectionMetadata();
        this.resolveLinks = false;
        this.startRevision = StreamRevision.START;
        this.direction = Direction.Forward;
        this.timeouts = Timeouts.DEFAULT;

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    public ReadStream authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public ReadStream forward() {
        this.direction = Direction.Forward;
        return this;
    }

    public ReadStream backward() {
        this.direction = Direction.Backward;
        return this;
    }

    public ReadStream timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public ReadStream requiresLeader() {
        return requiresLeader(true);
    }

    public ReadStream notRequireLeader() {
        return requiresLeader(false);
    }

    public ReadStream requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public ReadStream resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public ReadStream resolveLinks() {
        return this.resolveLinks(true);
    }

    public ReadStream notResolveLinks() {
        return this.resolveLinks(false);
    }

    public ReadStream startingPosition(StreamRevision startRevision) {
        this.startRevision = startRevision;
        return this;
    }

    public ReadStream fromStart() {
        return this.startingPosition(StreamRevision.START);
    }

    public ReadStream fromEnd() {
        return this.startingPosition(StreamRevision.END);
    }

    public ReadStream fromRevision(long revision) {
        return this.startingPosition(new StreamRevision(revision));
    }

    @Override
    public StreamsOuterClass.ReadReq.Options.Builder createOptions(long count) {
        return defaultReadOptions.clone()
                .setStream(GrpcUtils.toStreamOptions(this.streamName, this.startRevision))
                .setResolveLinks(this.resolveLinks)
                .setCount(count)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(this.direction == Direction.Forward ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);
    }
}

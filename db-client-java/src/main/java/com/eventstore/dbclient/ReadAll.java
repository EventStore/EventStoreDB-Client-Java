package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

public class ReadAll extends AbstractRead {
    private Position position;
    private Timeouts timeouts;
    private boolean resolveLinks;
    private Direction direction;

    public ReadAll(GrpcClient client, UserCredentials credentials) {
        super(client);

        this.metadata = new ConnectionMetadata();
        this.resolveLinks = false;
        this.position = Position.START;
        this.direction = Direction.Forward;
        this.timeouts = Timeouts.DEFAULT;

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    public ReadAll authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public ReadAll forward() {
        this.direction = Direction.Forward;
        return this;
    }

    public ReadAll backward() {
        this.direction = Direction.Backward;
        return this;
    }

    public ReadAll timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public ReadAll requiresLeader() {
        return requiresLeader(true);
    }

    public ReadAll notRequireLeader() {
        return requiresLeader(false);
    }

    public ReadAll requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public ReadAll resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public ReadAll resolveLinks() {
        return this.resolveLinks(true);
    }

    public ReadAll notResolveLinks() {
        return this.resolveLinks(false);
    }

    public ReadAll fromStart() {
        return this.fromPosition(Position.START);
    }

    public ReadAll fromEnd() {
        return this.fromPosition(Position.END);
    }

    public ReadAll fromPosition(Position position) {
        this.position = position;
        return this;
    }

    @Override
    public StreamsOuterClass.ReadReq.Options.Builder createOptions(long count) {
        return defaultReadOptions.clone()
                .setAll(StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder()
                        .setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                                .setCommitPosition(this.position.getCommitUnsigned())
                                .setPreparePosition(this.position.getPrepareUnsigned())))
                .setResolveLinks(this.resolveLinks)
                .setCount(count)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(this.direction == Direction.Forward ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);
    }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

public class SubscribeToAll extends AbstractRegularSubscription {
    private Position position;
    protected SubscriptionFilter filter;

    public SubscribeToAll(EventStoreDBConnection connection, SubscriptionListener listener, UserCredentials credentials) {
        super(connection);

        this.metadata = new ConnectionMetadata();
        this.resolveLinks = false;
        this.listener = listener;
        this.position = Position.START;

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    public SubscribeToAll authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public SubscribeToAll timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public SubscribeToAll requiresLeader() {
        return requiresLeader(true);
    }

    public SubscribeToAll notRequireLeader() {
        return requiresLeader(false);
    }

    public SubscribeToAll requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public SubscribeToAll resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public SubscribeToAll resolveLinks() {
        return this.resolveLinks(true);
    }

    public SubscribeToAll notResolveLinks() {
        return this.resolveLinks(false);
    }

    public SubscribeToAll fromStart() {
        return this.fromPosition(Position.START);
    }

    public SubscribeToAll fromEnd() {
        return this.fromPosition(Position.END);
    }

    public SubscribeToAll fromPosition(Position position) {
        this.position = position;
        return this;
    }

    public SubscribeToAll filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    protected StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        StreamsOuterClass.ReadReq.Options.Builder options =
        defaultSubscribeOptions.clone()
                .setResolveLinks(this.resolveLinks)
                .setAll(StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder()
                        .setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                                .setCommitPosition(this.position.getCommitUnsigned())
                                .setPreparePosition(this.position.getPrepareUnsigned()))
                        .build());

        if (this.filter != null) {
            this.filter.addToWireReadReq(options);
            this.checkpointer = this.filter.getCheckpointer();
        } else {
            options.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        return options;
    }
}

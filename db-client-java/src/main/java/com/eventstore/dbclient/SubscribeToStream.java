package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

public class SubscribeToStream extends AbstractRegularSubscription {
    private String streamName;
    private StreamRevision startRevision;

    public SubscribeToStream(EventStoreDBConnection connection, String streamName, SubscriptionListener listener, UserCredentials credentials) {
        super(connection);

        this.streamName = streamName;
        this.metadata = new ConnectionMetadata();
        this.resolveLinks = false;
        this.startRevision = StreamRevision.START;
        this.listener = listener;

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    public SubscribeToStream authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public SubscribeToStream timeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public SubscribeToStream requiresLeader() {
        return requiresLeader(true);
    }

    public SubscribeToStream notRequireLeader() {
        return requiresLeader(false);
    }

    public SubscribeToStream requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public SubscribeToStream resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public SubscribeToStream resolveLinks() {
        return this.resolveLinks(true);
    }

    public SubscribeToStream notResolveLinks() {
        return this.resolveLinks(false);
    }

    public SubscribeToStream startingPosition(StreamRevision startRevision) {
        this.startRevision = startRevision;
        return this;
    }

    public SubscribeToStream fromStart() {
        return this.startingPosition(StreamRevision.START);
    }

    public SubscribeToStream fromEnd() {
        return this.startingPosition(StreamRevision.END);
    }

    public SubscribeToStream fromRevision(long revision) {
        return this.startingPosition(new StreamRevision(revision));
    }

    @Override
    protected StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        return defaultSubscribeOptions.clone()
                .setResolveLinks(this.resolveLinks)
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setStream(GrpcUtils.toStreamOptions(this.streamName, this.startRevision));
    }
}

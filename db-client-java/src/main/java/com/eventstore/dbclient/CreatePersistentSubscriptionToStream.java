package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;

public class CreatePersistentSubscriptionToStream extends AbstractCreatePersistentSubscription {
    private final String stream;
    private final CreatePersistentSubscriptionOptions options;
    private final PersistentSubscriptionSettings settings;

    public CreatePersistentSubscriptionToStream(GrpcClient client, String stream, String group,
                                                CreatePersistentSubscriptionOptions options) {
        super(client, group, options);

        this.stream = stream;
        this.options = options;
        this.settings = options.getSettings();
    }

    @Override
    protected Persistent.CreateReq.Settings.Builder createSettings() {
        return Persistent.CreateReq.Settings.newBuilder()
                .setRevision(settings.getRevision());
    }

    @Override
    protected Persistent.CreateReq.Options.Builder createOptions() {
        PersistentSubscriptionSettings settings = options.getSettings();
        Persistent.CreateReq.Options.Builder optionsBuilder = Persistent.CreateReq.Options.newBuilder();
        Shared.StreamIdentifier.Builder streamIdentifierBuilder = Shared.StreamIdentifier.newBuilder();
        Persistent.CreateReq.StreamOptions.Builder streamOptionsBuilder = Persistent.CreateReq.StreamOptions
                .newBuilder();

        if (settings.getFromStart()) {
            streamOptionsBuilder.setStart(Shared.Empty.newBuilder());
        } else if (settings.getFromEnd()) {
            streamOptionsBuilder.setEnd(Shared.Empty.newBuilder());
        } else {
            streamOptionsBuilder.setRevision(settings.getRevision());
        }

        streamIdentifierBuilder.setStreamName(ByteString.copyFromUtf8(stream));
        streamOptionsBuilder.setStreamIdentifier(streamIdentifierBuilder);
        optionsBuilder.setStream(streamOptionsBuilder);
        optionsBuilder.setStreamIdentifier(streamIdentifierBuilder);

        return optionsBuilder;
    }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;

public class CreatePersistentSubscriptionToStream extends AbstractCreatePersistentSubscription {
    private final String stream;
    private final PersistentSubscriptionToStreamSettings settings;

    /**
     * @deprecated prefer {@link #CreatePersistentSubscriptionToStream(GrpcClient, String, String, CreatePersistentSubscriptionToStreamOptions)}
     */
    @Deprecated
    public CreatePersistentSubscriptionToStream(GrpcClient client, String stream, String group,
                                                CreatePersistentSubscriptionOptions options) {
        super(client, group, options.getSettings(), options.getMetadata());

        this.stream = stream;
        this.settings = PersistentSubscriptionToStreamSettings.copy(options.getSettings()).build();
    }

    public CreatePersistentSubscriptionToStream(GrpcClient client, String stream, String group,
                                                CreatePersistentSubscriptionToStreamOptions options) {
        super(client, group, options.getSettings(), options.getMetadata());

        this.stream = stream;
        this.settings = options.getSettings();
    }

    @Override
    protected Persistent.CreateReq.Settings.Builder createSettings() {
        return Persistent.CreateReq.Settings.newBuilder()
                .setRevision(settings.getRevision());
    }

    @Override
    protected Persistent.CreateReq.Options.Builder createOptions() {
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
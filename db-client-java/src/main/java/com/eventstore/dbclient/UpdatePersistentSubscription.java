package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;

public class UpdatePersistentSubscription extends AbstractUpdatePersistentSubscription {
    private final PersistentSubscriptionSettings settings;
    private String stream;

    public UpdatePersistentSubscription(GrpcClient connection, String stream, String group,
                                        UpdatePersistentSubscriptionOptions options) {
        super(connection, group, options.getSettings(), options.getMetadata());

        this.stream = stream;
        this.settings = options.getSettings();
    }

    @Override
    protected Persistent.UpdateReq.Settings.Builder createSettings() {
        return Persistent.UpdateReq.Settings.newBuilder()
                .setRevision(settings.getRevision());
    }

    @Override
    protected Persistent.UpdateReq.Options.Builder createOptions() {
        Persistent.UpdateReq.Options.Builder optionsBuilder = Persistent.UpdateReq.Options.newBuilder();
        Shared.StreamIdentifier.Builder streamIdentifierBuilder = Shared.StreamIdentifier.newBuilder();
        Persistent.UpdateReq.StreamOptions.Builder streamOptionsBuilder = Persistent.UpdateReq.StreamOptions
                .newBuilder();

        if (settings.getStreamRevision() == StreamRevision.START) {
            streamOptionsBuilder.setStart(Shared.Empty.newBuilder());
        } else if (settings.getStreamRevision() == StreamRevision.END) {
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

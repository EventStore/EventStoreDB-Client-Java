package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;

public class UpdatePersistentSubscriptionToAll extends AbstractUpdatePersistentSubscription {
    private final PersistentSubscriptionToAllSettings settings;

    public UpdatePersistentSubscriptionToAll(GrpcClient connection, String group,
                                             UpdatePersistentSubscriptionToAllOptions options) {
        super(connection, group, options.getSettings(), options.getMetadata());

        this.settings = options.getSettings();
    }

    @Override
    protected Persistent.UpdateReq.Options.Builder createOptions() {
        Persistent.UpdateReq.Options.Builder optionsBuilder = Persistent.UpdateReq.Options.newBuilder();
        Persistent.UpdateReq.AllOptions.Builder allOptionsBuilder = Persistent.UpdateReq.AllOptions.newBuilder();

        if (settings.getFromStart()) {
            allOptionsBuilder.setStart(Shared.Empty.newBuilder());
        } else if (settings.getFromEnd()) {
            allOptionsBuilder.setEnd(Shared.Empty.newBuilder());
        } else {
            Position position = settings.getPosition();
            allOptionsBuilder.setPosition(Persistent.UpdateReq.Position.newBuilder()
                    .setCommitPosition(position.getCommitUnsigned())
                    .setPreparePosition(position.getPrepareUnsigned()));
        }

        optionsBuilder.setAll(allOptionsBuilder);

        return optionsBuilder;
    }
}

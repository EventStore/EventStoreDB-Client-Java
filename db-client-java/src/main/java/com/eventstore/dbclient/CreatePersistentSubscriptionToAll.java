package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;

public class CreatePersistentSubscriptionToAll extends AbstractCreatePersistentSubscription {
    private final PersistentSubscriptionToAllSettings settings;

    public CreatePersistentSubscriptionToAll(GrpcClient client, String group,
                                             CreatePersistentSubscriptionToAllOptions options) {
        super(client, group, options.getSettings(), options.getMetadata());

        this.settings = options.getSettings();
    }

    @Override
    protected Persistent.CreateReq.Options.Builder createOptions() {
        Persistent.CreateReq.Options.Builder optionsBuilder = Persistent.CreateReq.Options.newBuilder();
        Persistent.CreateReq.AllOptions.Builder allOptionsBuilder = Persistent.CreateReq.AllOptions.newBuilder();

        if (settings.getFromStart()) {
            allOptionsBuilder.setStart(Shared.Empty.newBuilder());
        } else if (settings.getFromEnd()) {
            allOptionsBuilder.setEnd(Shared.Empty.newBuilder());
        } else {
            Position position = settings.getPosition();
            allOptionsBuilder.setPosition(Persistent.CreateReq.Position.newBuilder()
                    .setCommitPosition(position.getCommitUnsigned())
                    .setPreparePosition(position.getPrepareUnsigned()));
        }

        optionsBuilder.setAll(allOptionsBuilder);

        return optionsBuilder;
    }
}

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractUpdatePersistentSubscription {
    private final GrpcClient connection;
    private final String group;
    private final UpdatePersistentSubscriptionOptions options;

    public AbstractUpdatePersistentSubscription(GrpcClient connection, String group,
                                                UpdatePersistentSubscriptionOptions options) {
        this.connection = connection;
        this.group = group;
        this.options = options;
    }

    protected Persistent.UpdateReq.Settings.Builder createSettings() {
        return Persistent.UpdateReq.Settings.newBuilder();
    }

    protected abstract Persistent.UpdateReq.Options.Builder createOptions();

    public CompletableFuture execute() {
        return this.connection.run(channel -> {
            CompletableFuture result = new CompletableFuture();
            Metadata headers = this.options.getMetadata();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client = MetadataUtils
                    .attachHeaders(PersistentSubscriptionsGrpc.newStub(channel), headers);
            Persistent.UpdateReq.Settings.Builder settingsBuilder = createSettings();
            PersistentSubscriptionSettings settings = options.getSettings();

            settingsBuilder
                    .setResolveLinks(settings.isResolveLinks())
                    .setReadBatchSize(settings.getReadBatchSize())
                    .setMinCheckpointCount(settings.getMinCheckpointCount())
                    .setMaxCheckpointCount(settings.getMaxCheckpointCount())
                    .setMessageTimeoutMs(settings.getMessageTimeoutMs())
                    .setMaxSubscriberCount(settings.getMaxSubscriberCount())
                    .setMaxRetryCount(settings.getMaxRetryCount())
                    .setLiveBufferSize(settings.getLiveBufferSize())
                    .setHistoryBufferSize(settings.getHistoryBufferSize())
                    .setExtraStatistics(settings.isExtraStatistics())
                    .setCheckpointAfterMs(settings.getCheckpointAfterMs());

            switch (settings.getStrategy()) {
                case DispatchToSingle:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.DispatchToSingle);
                    break;
                case RoundRobin:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.RoundRobin);
                    break;
                case Pinned:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.Pinned);
                    break;
            }

            Persistent.UpdateReq req = Persistent.UpdateReq.newBuilder()
                    .setOptions(createOptions()
                            .setSettings(settingsBuilder)
                            .setGroupName(group))
                    .build();

            client.update(req, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

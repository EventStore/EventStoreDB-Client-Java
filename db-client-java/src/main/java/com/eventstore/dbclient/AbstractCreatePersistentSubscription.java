package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractCreatePersistentSubscription {
    private final GrpcClient client;
    private final String group;
    private final CreatePersistentSubscriptionOptions options;

    public AbstractCreatePersistentSubscription(GrpcClient client, String group,
                                                CreatePersistentSubscriptionOptions options) {
        this.client = client;
        this.group = group;
        this.options = options;
    }

    protected Persistent.CreateReq.Settings.Builder createSettings(){
        return Persistent.CreateReq.Settings.newBuilder();
    };

    protected abstract Persistent.CreateReq.Options.Builder createOptions();

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            CompletableFuture result = new CompletableFuture();
            Metadata headers = this.options.getMetadata();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client = MetadataUtils
                    .attachHeaders(PersistentSubscriptionsGrpc.newStub(channel), headers);
            Persistent.CreateReq.Settings.Builder settingsBuilder = createSettings();

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
                    settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.DispatchToSingle);
                    break;
                case RoundRobin:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.RoundRobin);
                    break;
                case Pinned:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.Pinned);
                    break;
            }

            Persistent.CreateReq req = Persistent.CreateReq.newBuilder()
                    .setOptions(createOptions()
                        .setSettings(settingsBuilder)
                        .setGroupName(group))
                    .build();

            client.create(req, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

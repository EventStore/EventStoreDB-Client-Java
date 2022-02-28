package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractUpdatePersistentSubscription {
    private final GrpcClient connection;
    private final String group;
    private final AbstractPersistentSubscriptionSettings settings;
    private Metadata metadata;

    public AbstractUpdatePersistentSubscription(GrpcClient connection, String group,
                                                AbstractPersistentSubscriptionSettings settings, Metadata metadata) {
        this.connection = connection;
        this.group = group;
        this.settings = settings;
        this.metadata = metadata;
    }

    protected Persistent.UpdateReq.Settings.Builder createSettings() {
        return Persistent.UpdateReq.Settings.newBuilder();
    }

    protected abstract Persistent.UpdateReq.Options.Builder createOptions();

    public CompletableFuture execute() {
        return this.connection.runWithArgs(args -> {
            CompletableFuture result = new CompletableFuture();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client = MetadataUtils
                    .attachHeaders(PersistentSubscriptionsGrpc.newStub(args.getChannel()), metadata);
            Persistent.UpdateReq.Settings.Builder settingsBuilder = createSettings();

            settingsBuilder
                    .setResolveLinks(settings.shouldResolveLinkTos())
                    .setReadBatchSize(settings.getReadBatchSize())
                    .setMinCheckpointCount(settings.getCheckPointLowerBound())
                    .setMaxCheckpointCount(settings.getCheckPointUpperBound())
                    .setMessageTimeoutMs(settings.getMessageTimeoutMs())
                    .setMaxSubscriberCount(settings.getMaxSubscriberCount())
                    .setMaxRetryCount(settings.getMaxRetryCount())
                    .setLiveBufferSize(settings.getLiveBufferSize())
                    .setHistoryBufferSize(settings.getHistoryBufferSize())
                    .setExtraStatistics(settings.isExtraStatistics())
                    .setCheckpointAfterMs(settings.getCheckpointAfterMs());

            switch (settings.getConsumerStrategyName()) {
                case NamedConsumerStrategy.DISPATCH_TO_SINGLE:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.DispatchToSingle);
                    break;
                case NamedConsumerStrategy.ROUND_ROBIN:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.RoundRobin);
                    break;
                case NamedConsumerStrategy.PINNED:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.Pinned);
                    break;
            }

            Persistent.UpdateReq req = Persistent.UpdateReq.newBuilder()
                    .setOptions(createOptions()
                            .setSettings(settingsBuilder)
                            .setGroupName(group))
                    .build();

            if (req.getOptions().hasAll() && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeature());
            } else {
                client.update(req, GrpcUtils.convertSingleResponse(result));
            }

            return result;
        });
    }
}

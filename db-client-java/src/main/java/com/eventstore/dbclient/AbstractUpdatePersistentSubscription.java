package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractUpdatePersistentSubscription {
    private final GrpcClient connection;
    private final String group;
    private final PersistentSubscriptionSettings settings;
    private final OptionsBase options;

    public AbstractUpdatePersistentSubscription(GrpcClient connection, String group,
                                                PersistentSubscriptionSettings settings, OptionsBase options) {
        this.connection = connection;
        this.group = group;
        this.settings = settings;
        this.options = options;
    }

    protected Persistent.UpdateReq.Settings.Builder createSettings() {
        return Persistent.UpdateReq.Settings.newBuilder();
    }

    protected abstract Persistent.UpdateReq.Options.Builder createOptions();

    @SuppressWarnings("unchecked")
    public CompletableFuture execute() {
        return this.connection.runWithArgs(args -> {
            CompletableFuture result = new CompletableFuture();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client =
                    GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), this.connection.getSettings(), this.options);
            Persistent.UpdateReq.Settings.Builder settingsBuilder = createSettings();

            settingsBuilder
                    .setResolveLinks(settings.shouldResolveLinkTos())
                    .setReadBatchSize((int)settings.getReadBatchSize())
                    .setMinCheckpointCount((int)settings.getCheckpointLowerBound())
                    .setMaxCheckpointCount((int)settings.getCheckpointUpperBound())
                    .setMessageTimeoutMs((int)settings.getMessageTimeoutMs())
                    .setMaxSubscriberCount((int)settings.getMaxSubscriberCount())
                    .setMaxRetryCount((int)settings.getMaxRetryCount())
                    .setLiveBufferSize((int)settings.getLiveBufferSize())
                    .setHistoryBufferSize((int)settings.getHistoryBufferSize())
                    .setExtraStatistics(settings.isExtraStatistics())
                    .setCheckpointAfterMs((int)settings.getCheckpointAfterInMs());

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

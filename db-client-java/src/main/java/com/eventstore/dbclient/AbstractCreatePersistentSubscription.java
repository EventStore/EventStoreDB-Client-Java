package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

abstract class AbstractCreatePersistentSubscription<TPos, TSettings extends PersistentSubscriptionSettings> {
    private final GrpcClient client;
    private final String group;
    private final TSettings settings;
    private final OptionsBase options;

    public AbstractCreatePersistentSubscription(GrpcClient client, String group,
                                                TSettings settings, OptionsBase options) {
        this.client = client;
        this.group = group;
        this.settings = settings;
        this.options = options;
    }

    protected Persistent.CreateReq.Settings.Builder createSettings(){
        return Persistent.CreateReq.Settings.newBuilder();
    };

    protected abstract Persistent.CreateReq.Options.Builder createOptions();

    @SuppressWarnings({"unchecked", "deprecation"})
    public CompletableFuture execute() {
        return this.client.runWithArgs(args -> {
            CompletableFuture result = new CompletableFuture();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client =
                    GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), this.client.getSettings(), this.options);

            Persistent.CreateReq.Settings.Builder settingsBuilder = createSettings();

            settingsBuilder
                    .setResolveLinks(settings.shouldResolveLinkTos())
                    .setReadBatchSize(settings.getReadBatchSize())
                    .setMinCheckpointCount(settings.getCheckpointLowerBound())
                    .setMaxCheckpointCount(settings.getCheckpointUpperBound())
                    .setMessageTimeoutMs(settings.getMessageTimeoutMs())
                    .setMaxSubscriberCount(settings.getMaxSubscriberCount())
                    .setMaxRetryCount(settings.getMaxRetryCount())
                    .setLiveBufferSize(settings.getLiveBufferSize())
                    .setHistoryBufferSize(settings.getHistoryBufferSize())
                    .setExtraStatistics(settings.isExtraStatistics())
                    .setCheckpointAfterMs(settings.getCheckpointAfterInMs());

            switch (settings.getConsumerStrategyName()) {
                case NamedConsumerStrategy.DISPATCH_TO_SINGLE:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.DispatchToSingle);
                    break;
                case NamedConsumerStrategy.ROUND_ROBIN:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.RoundRobin);
                    break;
                case NamedConsumerStrategy.PINNED:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.Pinned);
                    break;
            }

            Persistent.CreateReq req = Persistent.CreateReq.newBuilder()
                    .setOptions(createOptions()
                        .setSettings(settingsBuilder)
                        .setGroupName(group))
                    .build();

            if (req.getOptions().hasAll() && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeature());
            } else {
                client.create(req, GrpcUtils.convertSingleResponse(result));
            }

            return result;
        });
    }
}

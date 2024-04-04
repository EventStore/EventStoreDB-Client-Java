package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

abstract class AbstractCreatePersistentSubscription<TPos, TSettings extends PersistentSubscriptionSettings> {
    private final GrpcClient client;
    private final String group;
    private final TSettings settings;
    private final OptionsBase<?> options;
    private static final Logger logger = LoggerFactory.getLogger(AbstractCreatePersistentSubscription.class);

    public AbstractCreatePersistentSubscription(GrpcClient client, String group,
                                                TSettings settings, OptionsBase<?> options) {
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

            if (settings.getNamedConsumerStrategy().isDispatchToSingle()) {
                settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.DispatchToSingle);
            } else if (settings.getNamedConsumerStrategy().isRoundRobin()) {
                settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.RoundRobin);
            } else if (settings.getNamedConsumerStrategy().isPinned()) {
                settingsBuilder.setNamedConsumerStrategy(Persistent.CreateReq.ConsumerStrategy.Pinned);
            } else {
                logger.error(String.format("Unsupported named consumer strategy: '%s'", settings.getNamedConsumerStrategy().toString()));
                throw new UnsupportedFeatureException();
            }

            Persistent.CreateReq req = Persistent.CreateReq.newBuilder()
                    .setOptions(createOptions()
                        .setSettings(settingsBuilder)
                        .setGroupName(group))
                    .build();

            if (req.getOptions().hasAll() && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeatureException());
            } else {
                client.create(req, GrpcUtils.convertSingleResponse(result));
            }

            return result;
        });
    }
}

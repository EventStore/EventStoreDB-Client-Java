package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CreatePersistentSubscription {
    private final GrpcClient client;
    private final String stream;
    private final String group;
    private final CreatePersistentSubscriptionOptions options;

    public CreatePersistentSubscription(GrpcClient client, String stream, String group, CreatePersistentSubscriptionOptions options) {
        this.client = client;
        this.stream = stream;
        this.group = group;
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            CompletableFuture result = new CompletableFuture();
            Metadata headers = this.options.getMetadata();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client = MetadataUtils.attachHeaders(PersistentSubscriptionsGrpc.newStub(channel), headers);

            Persistent.CreateReq.Options.Builder builder = Persistent.CreateReq.Options.newBuilder();
            Persistent.CreateReq.Settings.Builder settingsBuilder = Persistent.CreateReq.Settings.newBuilder();
            Shared.StreamIdentifier.Builder streamIdentifierBuilder = Shared.StreamIdentifier.newBuilder();

            PersistentSubscriptionSettings settings = options.getSettings();
            settingsBuilder.setRevision(settings.getRevision())
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

            streamIdentifierBuilder.setStreamName(ByteString.copyFromUtf8(stream));

            builder.setSettings(settingsBuilder)
                    .setGroupName(group)
                    .setStreamIdentifier(streamIdentifierBuilder)
                    .build();

            Persistent.CreateReq req = Persistent.CreateReq.newBuilder()
                    .setOptions(builder)
                    .build();

            client.create(req, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

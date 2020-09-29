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
    private final EventStoreDBConnection connection;
    private final String stream;
    private final String group;
    private PersistentSubscriptionSettings settings;
    private ConnectionMetadata metadata;

    public CreatePersistentSubscription(EventStoreDBConnection connection, String stream, String group, UserCredentials credentials) {
        this.connection = connection;
        this.stream = stream;
        this.group = group;
        this.settings = PersistentSubscriptionSettings.builder().build();
        this.metadata = new ConnectionMetadata();

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    public CreatePersistentSubscription authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public CreatePersistentSubscription settings(PersistentSubscriptionSettings settings) {
        this.settings = settings;

        return this;
    }

    public CompletableFuture execute() {
        return this.connection.run(channel -> {
            CompletableFuture result = new CompletableFuture();
            Metadata headers = this.metadata.build();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client = MetadataUtils.attachHeaders(PersistentSubscriptionsGrpc.newStub(channel), headers);

            Persistent.CreateReq.Options.Builder builder = Persistent.CreateReq.Options.newBuilder();
            Persistent.CreateReq.Settings.Builder settingsBuilder = Persistent.CreateReq.Settings.newBuilder();
            Shared.StreamIdentifier.Builder streamIdentifierBuilder = Shared.StreamIdentifier.newBuilder();

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

            client.create(req, GrpcUtils.convertSingleResponse(result, Function.identity()));

            return result;
        });
    }
}

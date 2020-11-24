package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class UpdatePersistentSubscription {
    private final GrpcClient connection;
    private final String stream;
    private final String group;
    private PersistentSubscriptionSettings settings;
    private ConnectionMetadata metadata;

    public UpdatePersistentSubscription(GrpcClient connection, String stream, String group, UserCredentials credentials) {
        this.connection = connection;
        this.stream = stream;
        this.group = group;
        this.settings = PersistentSubscriptionSettings.builder().build();
        this.metadata = new ConnectionMetadata();

        if (credentials != null) {
            this.metadata.authenticated(credentials);
        }
    }

    public UpdatePersistentSubscription authenticated(UserCredentials credentials) {
        this.metadata.authenticated(credentials);
        return this;
    }

    public UpdatePersistentSubscription settings(PersistentSubscriptionSettings settings) {
        this.settings = settings;

        return this;
    }

    public CompletableFuture execute() {
        return this.connection.run(channel -> {
            CompletableFuture result = new CompletableFuture();
            Metadata headers = this.metadata.build();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client = MetadataUtils.attachHeaders(PersistentSubscriptionsGrpc.newStub(channel), headers);

            Persistent.UpdateReq.Options.Builder builder = Persistent.UpdateReq.Options.newBuilder();
            Persistent.UpdateReq.Settings.Builder settingsBuilder = Persistent.UpdateReq.Settings.newBuilder();
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
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.DispatchToSingle);
                    break;
                case RoundRobin:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.RoundRobin);
                    break;
                case Pinned:
                    settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.Pinned);
                    break;
            }

            streamIdentifierBuilder.setStreamName(ByteString.copyFromUtf8(stream));

            builder.setSettings(settingsBuilder)
                    .setGroupName(group)
                    .setStreamIdentifier(streamIdentifierBuilder)
                    .build();

            Persistent.UpdateReq req = Persistent.UpdateReq.newBuilder()
                    .setOptions(builder)
                    .build();

            client.update(req, GrpcUtils.convertSingleResponse(result, Function.identity()));

            return result;
        });
    }
}

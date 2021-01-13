package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DeletePersistentSubscription {
    private final GrpcClient client;
    private final String stream;
    private final String group;
    private final DeletePersistentSubscriptionOptions options;

    public DeletePersistentSubscription(GrpcClient client, String stream, String group, DeletePersistentSubscriptionOptions options) {
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

            Shared.StreamIdentifier streamIdentifier =
                    Shared.StreamIdentifier.newBuilder()
                            .setStreamName(ByteString.copyFromUtf8(stream))
                            .build();

            Persistent.DeleteReq.Options options = Persistent.DeleteReq.Options.newBuilder()
                    .setStreamIdentifier(streamIdentifier)
                    .setGroupName(group)
                    .build();

            Persistent.DeleteReq req = Persistent.DeleteReq.newBuilder()
                    .setOptions(options)
                    .build();

            client.delete(req, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

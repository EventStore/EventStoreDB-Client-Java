package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractDeletePersistentSubscription {
    private final GrpcClient client;
    private final String group;
    private final DeletePersistentSubscriptionOptions options;

    public AbstractDeletePersistentSubscription(GrpcClient client, String group, DeletePersistentSubscriptionOptions options) {
        this.client = client;
        this.group = group;
        this.options = options;
    }

    protected abstract Persistent.DeleteReq.Options.Builder createOptions();

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            CompletableFuture result = new CompletableFuture();
            Metadata headers = this.options.getMetadata();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client = MetadataUtils
                    .attachHeaders(PersistentSubscriptionsGrpc.newStub(channel), headers);

            Persistent.DeleteReq req = Persistent.DeleteReq.newBuilder()
                    .setOptions(createOptions()
                            .setGroupName(group))
                    .build();

            client.delete(req, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}

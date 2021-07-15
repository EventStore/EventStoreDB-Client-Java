package com.eventstore.dbclient;

public class DeletePersistentSubscription extends DeletePersistentSubscriptionToStream {
    public DeletePersistentSubscription(GrpcClient client, String stream, String group,
                                                DeletePersistentSubscriptionOptions options) {
        super(client, stream, group,options);
    }
}

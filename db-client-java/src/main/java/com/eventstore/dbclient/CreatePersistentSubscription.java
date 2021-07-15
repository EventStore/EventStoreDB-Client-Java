package com.eventstore.dbclient;

/**
 * @deprecated Prefer {@link CreatePersistentSubscriptionToStream}
 */
@Deprecated
public class CreatePersistentSubscription extends CreatePersistentSubscriptionToStream {
    public CreatePersistentSubscription(GrpcClient client, String stream, String group,
                                        CreatePersistentSubscriptionOptions options) {
        super(client, stream, group, options);
    }
}

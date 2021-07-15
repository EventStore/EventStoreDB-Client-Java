package com.eventstore.dbclient;

/**
 * @deprecated Prefer {@link UpdatePersistentSubscriptionToStream}
 */
@Deprecated
public class UpdatePersistentSubscription extends UpdatePersistentSubscriptionToStream {
    public UpdatePersistentSubscription(GrpcClient connection, String stream, String group,
                                        UpdatePersistentSubscriptionOptions options){
        super(connection, stream, group, options);
    }
}

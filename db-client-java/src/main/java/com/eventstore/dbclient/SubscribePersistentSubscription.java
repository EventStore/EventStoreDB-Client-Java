package com.eventstore.dbclient;

/**
 * @deprecated Prefer {@link SubscribePersistentSubscriptionToStream}
 */
@Deprecated
public class SubscribePersistentSubscription extends SubscribePersistentSubscriptionToStream {
    public SubscribePersistentSubscription(GrpcClient connection, String stream, String group,
                                                   SubscribePersistentSubscriptionOptions options,
                                                   PersistentSubscriptionListener listener){
        super(connection, stream, group, options, listener);
    }
}

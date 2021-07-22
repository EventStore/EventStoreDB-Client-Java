package com.eventstore.dbclient;

/**
 * @deprecated prefer {@link UpdatePersistentSubscriptionToStreamOptions}
 */
@Deprecated
public class UpdatePersistentSubscriptionOptions
        extends ManagePersistentSubscriptionOptionsBase<UpdatePersistentSubscriptionOptions, PersistentSubscriptionSettings> {
    protected UpdatePersistentSubscriptionOptions() {
        super(PersistentSubscriptionSettings.builder().build());
    }

    public static UpdatePersistentSubscriptionOptions get() {
        return new UpdatePersistentSubscriptionOptions();
    }
}

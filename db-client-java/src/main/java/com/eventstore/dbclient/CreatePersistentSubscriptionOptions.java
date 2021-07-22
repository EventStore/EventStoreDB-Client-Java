package com.eventstore.dbclient;

/**
 * @deprecated prefer {@link CreatePersistentSubscriptionToStreamOptions}
 */
@Deprecated
public class CreatePersistentSubscriptionOptions
        extends ManagePersistentSubscriptionOptionsBase<CreatePersistentSubscriptionOptions, PersistentSubscriptionSettings> {
    protected CreatePersistentSubscriptionOptions() {
        super(PersistentSubscriptionSettings.builder().build());
    }

    public static CreatePersistentSubscriptionOptions get() {
        return new CreatePersistentSubscriptionOptions();
    }
}

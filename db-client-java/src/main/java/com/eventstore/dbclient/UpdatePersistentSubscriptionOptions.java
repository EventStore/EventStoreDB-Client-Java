package com.eventstore.dbclient;

public class UpdatePersistentSubscriptionOptions extends ManagePersistentSubscriptionOptionsBase<UpdatePersistentSubscriptionOptions> {
    private UpdatePersistentSubscriptionOptions() {
    }

    public static UpdatePersistentSubscriptionOptions get() {
        return new UpdatePersistentSubscriptionOptions();
    }
}

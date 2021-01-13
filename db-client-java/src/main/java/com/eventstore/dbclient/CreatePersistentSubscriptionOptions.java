package com.eventstore.dbclient;

public class CreatePersistentSubscriptionOptions extends ManagePersistentSubscriptionOptionsBase<CreatePersistentSubscriptionOptions> {
    private CreatePersistentSubscriptionOptions() {
    }

    public static CreatePersistentSubscriptionOptions get() {
        return new CreatePersistentSubscriptionOptions();
    }
}

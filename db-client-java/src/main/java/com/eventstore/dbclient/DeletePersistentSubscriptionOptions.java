package com.eventstore.dbclient;

public class DeletePersistentSubscriptionOptions extends OptionsBase<DeletePersistentSubscriptionOptions> {
    private DeletePersistentSubscriptionOptions() {
    }

    public static DeletePersistentSubscriptionOptions get() {
        return new DeletePersistentSubscriptionOptions();
    }
}

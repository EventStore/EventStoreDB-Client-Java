package com.eventstore.dbclient;

public class UpdatePersistentSubscriptionOptions extends OptionsBase<UpdatePersistentSubscriptionOptions> {
    private PersistentSubscriptionSettings settings;

    private UpdatePersistentSubscriptionOptions() {
        this.settings = PersistentSubscriptionSettings.builder().build();
    }

    public static UpdatePersistentSubscriptionOptions get() {
        return new UpdatePersistentSubscriptionOptions();
    }

    public PersistentSubscriptionSettings getSettings() {
        return settings;
    }

    public UpdatePersistentSubscriptionOptions settings(PersistentSubscriptionSettings settings) {
        this.settings = settings;

        return this;
    }
}

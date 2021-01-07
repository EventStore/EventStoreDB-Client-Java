package com.eventstore.dbclient;

public class CreatePersistentSubscriptionOptions extends OptionsBase<CreatePersistentSubscriptionOptions> {
    private PersistentSubscriptionSettings settings;

    private CreatePersistentSubscriptionOptions() {
        this.settings = PersistentSubscriptionSettings.builder().build();
    }

    public static CreatePersistentSubscriptionOptions get() {
        return new CreatePersistentSubscriptionOptions();
    }

    public CreatePersistentSubscriptionOptions settings(PersistentSubscriptionSettings settings) {
        this.settings = settings;

        return this;
    }

    public PersistentSubscriptionSettings getSettings() {
        return settings;
    }
}

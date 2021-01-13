package com.eventstore.dbclient;

class ManagePersistentSubscriptionOptionsBase<T> extends OptionsBase<T> {
    private PersistentSubscriptionSettings settings;

    protected ManagePersistentSubscriptionOptionsBase() {
        this.settings = PersistentSubscriptionSettings.builder().build();
    }

    public T settings(PersistentSubscriptionSettings settings) {
        this.settings = settings;

        return (T)this;
    }

    public PersistentSubscriptionSettings getSettings() {
        return settings;
    }
}

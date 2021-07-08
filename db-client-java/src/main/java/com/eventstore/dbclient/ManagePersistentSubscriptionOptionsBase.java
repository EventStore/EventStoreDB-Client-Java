package com.eventstore.dbclient;

abstract class ManagePersistentSubscriptionOptionsBase<TO,TS> extends OptionsBase<TO> {
    private TS settings;

    protected ManagePersistentSubscriptionOptionsBase(TS settings)
    {
        this.settings = settings;
    }

    public TO settings(TS settings) {
        this.settings = settings;

        return (TO)this;
    }

    public TS getSettings() {
        return settings;
    }
}

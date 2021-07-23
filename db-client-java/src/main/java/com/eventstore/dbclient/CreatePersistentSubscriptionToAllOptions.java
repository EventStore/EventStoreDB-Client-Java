package com.eventstore.dbclient;

public class CreatePersistentSubscriptionToAllOptions
        extends ManagePersistentSubscriptionOptionsBase<CreatePersistentSubscriptionToAllOptions, PersistentSubscriptionToAllSettings> {
    private SubscriptionFilter filter;

    protected CreatePersistentSubscriptionToAllOptions() {
        super(PersistentSubscriptionToAllSettings.builder().build());
    }

    public static CreatePersistentSubscriptionToAllOptions get() {
        return new CreatePersistentSubscriptionToAllOptions();
    }

    public SubscriptionFilter getFilter() {
        return filter;
    }

    public CreatePersistentSubscriptionToAllOptions filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }
}

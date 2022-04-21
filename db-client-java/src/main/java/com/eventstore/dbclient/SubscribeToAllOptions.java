package com.eventstore.dbclient;

public class SubscribeToAllOptions extends OptionsWithPositionAndResolveLinkTosBase<SubscribeToAllOptions> {
    protected SubscriptionFilter filter;

    private SubscribeToAllOptions() {
        this.kind = OperationKind.Streaming;
    }

    public static SubscribeToAllOptions get() {
        return new SubscribeToAllOptions();
    }

    public SubscriptionFilter getFilter() {
        return filter;
    }

    public SubscribeToAllOptions filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }
}

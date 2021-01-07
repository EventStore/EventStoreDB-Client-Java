package com.eventstore.dbclient;

public class SubscribeToAllOptions extends OptionsBase<SubscribeToAllOptions> {
    private boolean resolveLinkTos;
    private Position position;
    protected SubscriptionFilter filter;

    private SubscribeToAllOptions() {
        this.resolveLinkTos = false;
        this.position = Position.START;
    }

    public static SubscribeToAllOptions get() {
        return new SubscribeToAllOptions();
    }

    public boolean shouldResolveLinkTos() {
        return this.resolveLinkTos;
    }

    public SubscribeToAllOptions resolveLinkTos(boolean value) {
        this.resolveLinkTos = value;
        return this;
    }

    public SubscribeToAllOptions resolveLinkTos() {
        return this.resolveLinkTos(true);
    }

    public SubscribeToAllOptions notResolveLinkTos() {
        return this.resolveLinkTos(false);
    }

    public Position getPosition() {
        return position;
    }

    public SubscribeToAllOptions fromStart() {
        return this.fromPosition(Position.START);
    }

    public SubscribeToAllOptions fromEnd() {
        return this.fromPosition(Position.END);
    }

    public SubscribeToAllOptions fromPosition(Position position) {
        this.position = position;
        return this;
    }

    public SubscriptionFilter getFilter() {
        return filter;
    }

    public SubscribeToAllOptions filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }
}

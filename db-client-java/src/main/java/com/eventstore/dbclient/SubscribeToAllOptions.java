package com.eventstore.dbclient;

public class SubscribeToAllOptions extends OptionsBase<SubscribeToAllOptions> {
    private boolean resolveLinks;
    private Position position;
    protected SubscriptionFilter filter;

    private SubscribeToAllOptions() {
        this.resolveLinks = false;
        this.position = Position.START;
    }

    public static SubscribeToAllOptions get() {
        return new SubscribeToAllOptions();
    }

    public boolean getResolveLinks() {
        return resolveLinks;
    }

    public SubscribeToAllOptions resolveLinks() {
        return this.resolveLinks(true);
    }

    public SubscribeToAllOptions notResolveLinks() {
        return this.resolveLinks(false);
    }

    public SubscribeToAllOptions resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
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

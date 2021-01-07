package com.eventstore.dbclient;

class OptionsWithPositionAndResolveLinkTosBase<T> extends OptionsWithResolveLinkTosBase<T> {
    private Position position;

    protected OptionsWithPositionAndResolveLinkTosBase() {
        this.position = Position.START;
    }

    public Position getPosition() {
        return position;
    }

    public T fromStart() {
        return this.fromPosition(Position.START);
    }

    public T fromEnd() {
        return this.fromPosition(Position.END);
    }

    public T fromPosition(Position position) {
        this.position = position;
        return (T)this;
    }
}

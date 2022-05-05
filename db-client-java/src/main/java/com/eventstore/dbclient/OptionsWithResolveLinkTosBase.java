package com.eventstore.dbclient;

class OptionsWithResolveLinkTosBase<T> extends OptionsBase<T> {
    private boolean resolveLinkTos;

    protected OptionsWithResolveLinkTosBase() {
        this.resolveLinkTos = false;
    }

    public boolean shouldResolveLinkTos() {
        return this.resolveLinkTos;
    }

    @SuppressWarnings("unchecked")
    public T resolveLinkTos(boolean value) {
        this.resolveLinkTos = value;
        return (T)this;
    }

    public T resolveLinkTos() {
        return this.resolveLinkTos(true);
    }

    public T notResolveLinkTos() {
        return this.resolveLinkTos(false);
    }
}

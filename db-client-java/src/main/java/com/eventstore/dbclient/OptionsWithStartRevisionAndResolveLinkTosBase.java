package com.eventstore.dbclient;

class OptionsWithStartRevisionAndResolveLinkTosBase<T> extends OptionsWithResolveLinkTosBase<T> {
    private StreamRevision startRevision;

    protected OptionsWithStartRevisionAndResolveLinkTosBase() {
        this.startRevision = StreamRevision.START;
    }

    public StreamRevision getStartingRevision() {
        return this.startRevision;
    }

    @SuppressWarnings("unchecked")
    public T fromRevision(StreamRevision startRevision) {
        this.startRevision = startRevision;
        return (T)this;
    }

    public T fromStart() {
        return this.fromRevision(StreamRevision.START);
    }

    public T fromEnd() {
        return this.fromRevision(StreamRevision.END);
    }

    public T fromRevision(long revision) {
        return this.fromRevision(new StreamRevision(revision));
    }
}

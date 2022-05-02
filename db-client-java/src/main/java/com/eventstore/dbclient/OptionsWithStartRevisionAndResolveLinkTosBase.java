package com.eventstore.dbclient;

class OptionsWithStartRevisionAndResolveLinkTosBase<T> extends OptionsWithResolveLinkTosBase<T> {
    private StreamPosition<Long> startRevision;

    protected OptionsWithStartRevisionAndResolveLinkTosBase(OperationKind kind) {
        super(kind);
        this.startRevision = StreamPosition.start();
    }

    protected OptionsWithStartRevisionAndResolveLinkTosBase() {
        this(OperationKind.Regular);
    }

    StreamPosition<Long> getStartingRevision() {
        return this.startRevision;
    }

    @SuppressWarnings("unchecked")
    public T fromRevision(StreamPosition<Long> startRevision) {
        this.startRevision = startRevision;
        return (T)this;
    }

    /**
     * Starts the subscription from the beginning of the stream.
     */
    public T fromStart() {
        return this.fromRevision(StreamPosition.start());
    }

    /**
     * Starts the subscription from the end of the stream.
     */
    public T fromEnd() {
        return this.fromRevision(StreamPosition.end());
    }

    /**
     * Starts the subscription from the given event revision.
     */
    public T fromRevision(long revision) {
        return this.fromRevision(StreamPosition.position(revision));
    }
}

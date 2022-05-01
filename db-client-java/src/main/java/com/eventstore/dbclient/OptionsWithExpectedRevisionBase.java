package com.eventstore.dbclient;

class OptionsWithExpectedRevisionBase<T> extends OptionsBase<T> {
    private ExpectedRevision expectedRevision;

    protected OptionsWithExpectedRevisionBase() {
        this.expectedRevision = ExpectedRevision.ANY;
    }

    public ExpectedRevision getExpectedRevision() {
        return this.expectedRevision;
    }

    @SuppressWarnings("unchecked")
    public T expectedRevision(ExpectedRevision revision) {
        this.expectedRevision = revision;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T expectedRevision(StreamRevision revision) {
        this.expectedRevision = ExpectedRevision.expectedRevision(revision.getValueUnsigned());
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T expectedRevision(long revision) {
        this.expectedRevision = ExpectedRevision.expectedRevision(revision);
        return (T) this;
    }
}

package com.eventstore.dbclient;

public class AppendToStreamOptions extends OptionsBase<AppendToStreamOptions> {
    private ExpectedRevision expectedRevision;

    private AppendToStreamOptions() {
        this.expectedRevision = ExpectedRevision.ANY;
    }

    public static AppendToStreamOptions get() {
        return new AppendToStreamOptions();
    }

    public ExpectedRevision getExpectedRevision() {
        return this.expectedRevision;
    }

    public AppendToStreamOptions expectedRevision(ExpectedRevision revision) {
        this.expectedRevision = revision;
        return this;
    }
}

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

    public AppendToStreamOptions requiresLeader() {
        return requiresLeader(true);
    }

    public AppendToStreamOptions notRequireLeader() {
        return requiresLeader(false);
    }

    public AppendToStreamOptions requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }
}

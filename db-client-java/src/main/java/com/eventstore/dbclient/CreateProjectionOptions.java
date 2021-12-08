package com.eventstore.dbclient;

public class CreateProjectionOptions extends OptionsBase<CreateProjectionOptions> {
    private boolean trackEmittedStreams;
    private boolean emitEnabled;

    private CreateProjectionOptions() {
        this.trackEmittedStreams = false;
    }

    public static CreateProjectionOptions get() {
        return new CreateProjectionOptions();
    }

    public boolean isTrackingEmittedStreams() {
        return trackEmittedStreams;
    }

    public boolean isEmitEnabled() {
        return emitEnabled;
    }

    public CreateProjectionOptions trackEmittedStreams(boolean trackEmittedStreams) {
        this.trackEmittedStreams = trackEmittedStreams;
        return this;
    }
    public CreateProjectionOptions emitEnabled(boolean value) {
        this.emitEnabled = value;
        return this;
    }
}

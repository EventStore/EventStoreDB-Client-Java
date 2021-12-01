package com.eventstore.dbclient;

public class CreateProjectionOptions extends OptionsBase<CreateProjectionOptions> {
    private boolean trackEmittedStreams;

    private CreateProjectionOptions() {
        this.trackEmittedStreams = false;
    }

    public static CreateProjectionOptions get() {
        return new CreateProjectionOptions();
    }

    public boolean isTrackingEmittedStreams() {
        return trackEmittedStreams;
    }

    public void trackEmittedStreams(boolean trackEmittedStreams) {
        this.trackEmittedStreams = trackEmittedStreams;
    }
}

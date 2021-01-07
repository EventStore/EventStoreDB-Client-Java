package com.eventstore.dbclient;

public class CreateContinuousProjectionOptions extends OptionsBase<CreateContinuousProjectionOptions> {
    private boolean trackEmittedStreams;

    private CreateContinuousProjectionOptions() {
        this.trackEmittedStreams = false;
    }

    public static CreateContinuousProjectionOptions get() {
        return new CreateContinuousProjectionOptions();
    }

    public boolean getTrackEmittedStreams() {
        return trackEmittedStreams;
    }

    public void trackEmittedStreams(boolean trackEmittedStreams) {
        this.trackEmittedStreams = trackEmittedStreams;
    }
}

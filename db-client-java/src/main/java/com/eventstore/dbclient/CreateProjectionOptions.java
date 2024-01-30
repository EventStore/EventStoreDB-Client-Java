package com.eventstore.dbclient;

/**
 * Options for create projection request.
 */
public class CreateProjectionOptions extends OptionsBase<CreateProjectionOptions> {
    private boolean trackEmittedStreams;
    private boolean checkpointsEnabled;
    private boolean enabled;
    private boolean emitEnabled;

    private CreateProjectionOptions() {
        this.trackEmittedStreams = false;
        this.checkpointsEnabled = false;
        this.enabled = true;
    }

    /**
     * Returns options with default values.
     */
    public static CreateProjectionOptions get() {
        return new CreateProjectionOptions();
    }

    boolean isTrackingEmittedStreams() {
        return trackEmittedStreams;
    }

    boolean isEmitEnabled() {
        return emitEnabled;
    }

    boolean isCheckpointsEnabled() {
        return checkpointsEnabled;
    }

    boolean isEnabled() {
        return enabled;
    }

    /**
     * If true, the projection tracks all streams it creates.
     */
    public CreateProjectionOptions trackEmittedStreams(boolean trackEmittedStreams) {
        this.trackEmittedStreams = trackEmittedStreams;
        return this;
    }

    /**
     * If true, allows the projection to emit events.
     */
    public CreateProjectionOptions emitEnabled(boolean value) {
        this.emitEnabled = value;
        return this;
    }

    /**
     * If true, store the progress of a projection in the streams it is processing
     */
    public CreateProjectionOptions checkpointsEnabled(boolean checkpointsEnabled) {
        this.checkpointsEnabled = checkpointsEnabled;
        return this;
    }

    /**
     * If set to true, the projection will be enabled immediately upon creation.
     */
    public CreateProjectionOptions enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}

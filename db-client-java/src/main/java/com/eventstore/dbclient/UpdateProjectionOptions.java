package com.eventstore.dbclient;

public class UpdateProjectionOptions extends OptionsBase<UpdateProjectionOptions> {
    private boolean emitEnabled;

    private UpdateProjectionOptions() {
    }

    public static UpdateProjectionOptions get() {
        return new UpdateProjectionOptions();
    }

    public UpdateProjectionOptions emitEnabled(boolean value) {
        this.emitEnabled = value;
        return this;
    }

    public boolean isEmitEnabled() {
        return emitEnabled;
    }
}

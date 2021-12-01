package com.eventstore.dbclient;

public class UpdateProjectionOptions extends OptionsBase<UpdateProjectionOptions> {
    private UpdateProjectionOptions() {
    }

    public static UpdateProjectionOptions get() {
        return new UpdateProjectionOptions();
    }
}

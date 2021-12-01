package com.eventstore.dbclient;

public class GetProjectionStateOptions extends OptionsBase<GetProjectionStateOptions> {
    private GetProjectionStateOptions() {
    }

    public static GetProjectionStateOptions get() {
        return new GetProjectionStateOptions();
    }
}

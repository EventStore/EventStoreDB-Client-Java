package com.eventstore.dbclient;

public class GetProjectionStatusOptions extends OptionsBase<GetProjectionStatusOptions> {
    private GetProjectionStatusOptions() {
    }

    public static GetProjectionStatusOptions get() {
        return new GetProjectionStatusOptions();
    }
}

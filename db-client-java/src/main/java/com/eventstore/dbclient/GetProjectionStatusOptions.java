package com.eventstore.dbclient;

/**
 * Options of the get projection status request.
 */
public class GetProjectionStatusOptions extends CallOptionsBase<GetProjectionStatusOptions> {
    private GetProjectionStatusOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static GetProjectionStatusOptions get() {
        return new GetProjectionStatusOptions();
    }
}

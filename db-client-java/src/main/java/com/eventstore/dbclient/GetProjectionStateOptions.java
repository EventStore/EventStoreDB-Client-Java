package com.eventstore.dbclient;

public class GetProjectionStateOptions extends OptionsBase<GetProjectionStateOptions> {
    private String partition;

    private GetProjectionStateOptions() {
        this.partition = "";
    }

    public static GetProjectionStateOptions get() {
        return new GetProjectionStateOptions();
    }

    public GetProjectionStateOptions partition(String partition) {
        this.partition = partition;
        return this;
    }

    public String getPartition() {
        return this.partition;
    }
}

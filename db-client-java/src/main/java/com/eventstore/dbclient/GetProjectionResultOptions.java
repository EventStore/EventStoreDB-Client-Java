package com.eventstore.dbclient;

public class GetProjectionResultOptions extends OptionsBase<GetProjectionResultOptions> {
    private String partition;

    public GetProjectionResultOptions() {
        this.partition = "";
    }

    public static GetProjectionResultOptions get() {
        return new GetProjectionResultOptions();
    }

    public String getPartition() {
        return this.partition;
    }

    public GetProjectionResultOptions partition(String partition) {
        this.partition = partition;
        return this;
    }
}

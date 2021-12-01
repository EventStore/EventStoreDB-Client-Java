package com.eventstore.dbclient;

public class GetProjectionStatisticsOptions extends OptionsBase<GetProjectionStatisticsOptions> {
    private GetProjectionStatisticsOptions() {
    }

    public static GetProjectionStatisticsOptions get() {
        return new GetProjectionStatisticsOptions();
    }
}

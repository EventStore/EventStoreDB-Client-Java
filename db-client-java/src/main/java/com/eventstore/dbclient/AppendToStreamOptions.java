package com.eventstore.dbclient;

public class AppendToStreamOptions extends OptionsWithExpectedRevisionBase<AppendToStreamOptions> {
    private AppendToStreamOptions() {
    }

    public static AppendToStreamOptions get() {
        return new AppendToStreamOptions();
    }
}

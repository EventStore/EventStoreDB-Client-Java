package com.eventstore.dbclient;

public class ListProjectionsOptions extends OptionsBase<ListProjectionsOptions> {
    private ListProjectionsOptions() {
    }

    public static ListProjectionsOptions get() {
        return new ListProjectionsOptions();
    }
}

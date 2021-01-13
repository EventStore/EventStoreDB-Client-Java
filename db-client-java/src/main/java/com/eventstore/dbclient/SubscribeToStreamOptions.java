package com.eventstore.dbclient;

public class SubscribeToStreamOptions extends OptionsWithStartRevisionAndResolveLinkTosBase<SubscribeToStreamOptions> {
    private SubscribeToStreamOptions() {
    }

    public static SubscribeToStreamOptions get() {
        return new SubscribeToStreamOptions();
    }
}

package com.eventstore.dbclient;

public class SubscribeToStreamOptions extends OptionsWithStartRevisionAndResolveLinkTosBase<SubscribeToStreamOptions> {
    private SubscribeToStreamOptions() {
        this.kind = OperationKind.Streaming;
    }

    public static SubscribeToStreamOptions get() {
        return new SubscribeToStreamOptions();
    }
}

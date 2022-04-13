package com.eventstore.dbclient;

public class SubscribePersistentSubscriptionOptions extends OptionsBase<SubscribePersistentSubscriptionOptions> {
    private int bufferSize;

    private SubscribePersistentSubscriptionOptions() {
        this.bufferSize = 10;
        this.kind = OperationKind.Streaming;
    }

    public static SubscribePersistentSubscriptionOptions get() {
        return new SubscribePersistentSubscriptionOptions();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public SubscribePersistentSubscriptionOptions setBufferSize(int value) {
        bufferSize = value;
        return this;
    }
}

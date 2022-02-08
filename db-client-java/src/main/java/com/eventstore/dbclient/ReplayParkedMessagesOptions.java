package com.eventstore.dbclient;

import java.time.Duration;

public class ReplayParkedMessagesOptions extends OptionsBase<ReplayParkedMessagesOptions> {
    Long stopAt = null;

    public Long getStopAt() {
        return stopAt;
    }

    public ReplayParkedMessagesOptions stopAt(long value) {
        this.stopAt = value;
        return this;
    }

    public static ReplayParkedMessagesOptions get() {
        return new ReplayParkedMessagesOptions();
    }
}

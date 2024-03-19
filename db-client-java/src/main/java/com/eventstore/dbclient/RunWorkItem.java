package com.eventstore.dbclient;

class RunWorkItem implements Msg {
    final String msgId;
    final AuthOptionsBase authOptions;
    final WorkItem item;

    public RunWorkItem(String msgId, AuthOptionsBase authOptions, WorkItem item) {
        this.msgId = msgId;
        this.authOptions = authOptions;
        this.item = item;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public WorkItem getItem() {
        return this.item;
    }

    public AuthOptionsBase getAuthOptions() { return this.authOptions; }

    public void reportError(Exception e) {
        this.item.accept(null, e);
    }

    @Override
    public String toString() {
        return "RunWorkItem[" + this.msgId + "]";
    }

    @Override
    public void accept(ConnectionService handler) {
        handler.process(this);
    }
}

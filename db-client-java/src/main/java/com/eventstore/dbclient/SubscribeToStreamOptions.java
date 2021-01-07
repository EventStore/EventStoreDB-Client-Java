package com.eventstore.dbclient;

public class SubscribeToStreamOptions extends OptionsBase<SubscribeToStreamOptions> {
    private StreamRevision startRevision;
    private boolean resolveLinkTos;

    private SubscribeToStreamOptions() {
        this.resolveLinkTos = false;
        this.startRevision = StreamRevision.START;
    }

    public static SubscribeToStreamOptions get() {
        return new SubscribeToStreamOptions();
    }

    public boolean shouldResolveLinkTos() {
        return this.resolveLinkTos;
    }

    public SubscribeToStreamOptions resolveLinkTos(boolean value) {
        this.resolveLinkTos = value;
        return this;
    }

    public SubscribeToStreamOptions resolveLinkTos() {
        return this.resolveLinkTos(true);
    }

    public SubscribeToStreamOptions notResolveLinkTos() {
        return this.resolveLinkTos(false);
    }

    public StreamRevision getStartingRevision() {
        return this.startRevision;
    }

    public SubscribeToStreamOptions startingRevision(StreamRevision startRevision) {
        this.startRevision = startRevision;
        return this;
    }

    public SubscribeToStreamOptions fromStart() {
        return this.startingRevision(StreamRevision.START);
    }

    public SubscribeToStreamOptions fromEnd() {
        return this.startingRevision(StreamRevision.END);
    }

    public SubscribeToStreamOptions fromRevision(long revision) {
        return this.startingRevision(new StreamRevision(revision));
    }


}

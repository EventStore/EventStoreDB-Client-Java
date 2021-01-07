package com.eventstore.dbclient;

public class SubscribeToStreamOptions extends OptionsBase<SubscribeToStreamOptions> {
    private StreamRevision startRevision;
    private boolean resolveLinks;

    private SubscribeToStreamOptions() {
        this.resolveLinks = false;
        this.startRevision = StreamRevision.START;
    }

    public static SubscribeToStreamOptions get() {
        return new SubscribeToStreamOptions();
    }

    public boolean getResolveLinks() {
        return this.resolveLinks;
    }

    public SubscribeToStreamOptions resolveLinks() {
        return this.resolveLinks(true);
    }

    public SubscribeToStreamOptions notResolveLinks() {
        return this.resolveLinks(false);
    }

    public SubscribeToStreamOptions resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
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

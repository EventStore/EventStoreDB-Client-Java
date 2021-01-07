package com.eventstore.dbclient;

public class ReadStreamOptions extends OptionsBase<ReadStreamOptions> {
    private StreamRevision startRevision;
    private boolean resolveLinkTos;
    private Direction direction;

    private ReadStreamOptions() {
        this.startRevision = StreamRevision.START;
        this.resolveLinkTos = false;
        this.direction = Direction.Forward;
    }

    public static ReadStreamOptions get() {
        return new ReadStreamOptions();
    }
    public Direction getDirection() {
        return this.direction;
    }

    public ReadStreamOptions forward() {
        this.direction = Direction.Forward;
        return this;
    }

    public ReadStreamOptions backward() {
        this.direction = Direction.Backward;
        return this;
    }

    public boolean shouldResolveLinkTos() {
        return this.resolveLinkTos;
    }

    public ReadStreamOptions resolveLinkTos(boolean value) {
        this.resolveLinkTos = value;
        return this;
    }

    public ReadStreamOptions resolveLinkTos() {
        return this.resolveLinkTos(true);
    }

    public ReadStreamOptions notResolveLinkTos() {
        return this.resolveLinkTos(false);
    }

    public StreamRevision getStartingRevision() {
        return this.startRevision;
    }

    public ReadStreamOptions startingRevision(StreamRevision startRevision) {
        this.startRevision = startRevision;
        return this;
    }

    public ReadStreamOptions fromStart() {
        return this.startingRevision(StreamRevision.START);
    }

    public ReadStreamOptions fromEnd() {
        return this.startingRevision(StreamRevision.END);
    }

    public ReadStreamOptions fromRevision(long revision) {
        return this.startingRevision(new StreamRevision(revision));
    }
}

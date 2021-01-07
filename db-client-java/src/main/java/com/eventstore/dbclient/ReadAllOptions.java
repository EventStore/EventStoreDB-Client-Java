package com.eventstore.dbclient;

public class ReadAllOptions extends OptionsBase<ReadAllOptions> {
    private Position position;
    private boolean resolveLinkTos;
    private Direction direction;

    private ReadAllOptions() {
        this.resolveLinkTos = false;
        this.position = Position.START;
        this.direction = Direction.Forward;
    }

    public static ReadAllOptions get() {
        return new ReadAllOptions();
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ReadAllOptions forward() {
        this.direction = Direction.Forward;
        return this;
    }

    public ReadAllOptions backward() {
        this.direction = Direction.Backward;
        return this;
    }

    public boolean shouldResolveLinkTos() {
        return this.resolveLinkTos;
    }

    public ReadAllOptions resolveLinkTos(boolean value) {
        this.resolveLinkTos = value;
        return this;
    }

    public ReadAllOptions resolveLinkTos() {
        return this.resolveLinkTos(true);
    }

    public ReadAllOptions notResolveLinkTos() {
        return this.resolveLinkTos(false);
    }

    public Position getPosition() {
        return this.position;
    }

    public ReadAllOptions fromStart() {
        return this.fromPosition(Position.START);
    }

    public ReadAllOptions fromEnd() {
        return this.fromPosition(Position.END);
    }

    public ReadAllOptions fromPosition(Position position) {
        this.position = position;
        return this;
    }
}

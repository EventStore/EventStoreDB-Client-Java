package com.eventstore.dbclient;

public class ReadAllOptions extends OptionsBase<ReadAllOptions> {
    private Position position;
    private boolean resolveLinks;
    private Direction direction;

    private ReadAllOptions() {
        this.resolveLinks = false;
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

    public ReadAllOptions requiresLeader() {
        return requiresLeader(true);
    }

    public ReadAllOptions notRequireLeader() {
        return requiresLeader(false);
    }

    public ReadAllOptions requiresLeader(boolean value) {
        if (value) {
            this.metadata.requiresLeader();
        }

        return this;
    }

    public boolean getResolveLinks() {
        return this.resolveLinks;
    }

    public ReadAllOptions resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public ReadAllOptions resolveLinks() {
        return this.resolveLinks(true);
    }

    public ReadAllOptions notResolveLinks() {
        return this.resolveLinks(false);
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

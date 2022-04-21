package com.eventstore.dbclient;

public class ReadAllOptions extends OptionsWithPositionAndResolveLinkTosBase<ReadAllOptions> {
    private Direction direction;

    private ReadAllOptions() {
        this.direction = Direction.Forwards;
        this.kind = OperationKind.Streaming;
    }

    public static ReadAllOptions get() {
        return new ReadAllOptions();
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ReadAllOptions direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    public ReadAllOptions forwards() {
        return direction(Direction.Forwards);
    }

    public ReadAllOptions backwards() {
        return direction(Direction.Backwards);
    }
}

package com.eventstore.dbclient;

public class ReadAllOptions extends OptionsWithPositionAndResolveLinkTosBase<ReadAllOptions> {
    private Direction direction;

    private ReadAllOptions() {
        this.direction = Direction.Forward;
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

    public ReadAllOptions forward() {
        return direction(Direction.Forward);
    }

    public ReadAllOptions backward() {
        return direction(Direction.Backward);
    }
}

package com.eventstore.dbclient;

public class ReadStreamOptions extends OptionsWithStartRevisionAndResolveLinkTosBase<ReadStreamOptions> {
    private Direction direction;

    private ReadStreamOptions() {
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
}

package com.eventstore.dbclient;

public class ReadStreamOptions extends OptionsWithStartRevisionAndResolveLinkTosBase<ReadStreamOptions> {
    private Direction direction;

    private ReadStreamOptions() {
        this.direction = Direction.Forwards;
        this.kind = OperationKind.Streaming;
    }

    public static ReadStreamOptions get() {
        return new ReadStreamOptions();
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ReadStreamOptions forwards() {
        this.direction = Direction.Forwards;
        return this;
    }

    public ReadStreamOptions backwards() {
        this.direction = Direction.Backwards;
        return this;
    }
}

package com.eventstore.dbclient;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class StreamRevision implements Comparable<StreamRevision> {
    public final static StreamRevision START = new StreamRevision(0);
    public final static StreamRevision END = new StreamRevision(-1);

    private final long value;

    public StreamRevision(long valueUnsigned) {
        this.value = valueUnsigned;
    }

    public StreamRevision(String value) {
        this.value = Long.parseUnsignedLong(value);
    }

    public long getValueUnsigned() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamRevision that = (StreamRevision) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Long.toUnsignedString(this.value);
    }

    @Override
    public int compareTo(@NotNull StreamRevision other) {
        return Long.compareUnsigned(this.value, other.value);
    }
}

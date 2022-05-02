package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

import java.util.Objects;

/**
 * Constants used for expected revision control.
 * <p>
 * The use of expected revision can be a bit tricky especially when discussing guaranties given by the
 * EventStoreDB server.
 *
 * The EventStoreDB server will assure idempotency for all requests using any value in <i>ExpectedRevision</i> except
 * <i>ANY</i>. When using <i>ANY</i>, the EventStoreDB server will do its best to assure idempotency but will not
 * guarantee it.
 * </p>
 */
public abstract class ExpectedRevision {
    /**
     * This write should not conflict with anything and should always succeed.
     */
    public static ExpectedRevision any() {
        return new AnyExpectedRevision();
    }

    /**
     * The stream being written to should not yet exist. If it does exist, treats that as a concurrency problem.
     */
    public static ExpectedRevision noStream() {
        return new NoStreamExpectedRevision();
    }

    /**
     * The stream should exist. If it or a metadata stream does not exist, treats that as a concurrency problem.
     */
    public static ExpectedRevision streamExists() {
        return new StreamExistsExpectedRevision();
    }

    /**
     * States that the last event written to the stream should have an event revision matching your expected value.
     */
    public static ExpectedRevision expectedRevision(long revision) {
        return new SpecificExpectedRevision(revision);
    }

    ExpectedRevision() {}

    abstract StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options);
    abstract StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options);
    abstract StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    static class NoStreamExpectedRevision extends ExpectedRevision {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setNoStream(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options) {
            return options.setNoStream(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options) {
            return options.setNoStream(Shared.Empty.getDefaultInstance());
        }
    }

    static class AnyExpectedRevision extends ExpectedRevision {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setAny(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options) {
            return options.setAny(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options) {
            return options.setAny(Shared.Empty.getDefaultInstance());
        }
    }

    static class StreamExistsExpectedRevision extends ExpectedRevision {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setStreamExists(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options) {
            return options.setStreamExists(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options) {
            return options.setStreamExists(Shared.Empty.getDefaultInstance());
        }
    }

    static class SpecificExpectedRevision extends ExpectedRevision {
        final long version;

        SpecificExpectedRevision(long version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SpecificExpectedRevision that = (SpecificExpectedRevision) o;
            return version == that.version;
        }

        @Override
        public int hashCode() {
            return Objects.hash(version);
        }

        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setRevision(version);
        }

        @Override
        public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options) {
            return options.setRevision(version);
        }

        @Override
        public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options) {
            return options.setRevision(version);
        }
    }
}

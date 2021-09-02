package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

import java.util.Objects;

public abstract class ExpectedRevision {
    public final static ExpectedRevision ANY = new AnyExpectedRevision();
    public final static ExpectedRevision NO_STREAM = new NoStreamExpectedRevision();
    public final static ExpectedRevision STREAM_EXISTS = new StreamExistsExpectedRevision();
    public static ExpectedRevision expectedRevision(long revision) {
        return new SpecificExpectedRevision(revision);
    }

    abstract public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options);
    abstract public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options);
    abstract public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options);

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

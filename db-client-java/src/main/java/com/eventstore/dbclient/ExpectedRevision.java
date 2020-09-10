package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

public abstract class ExpectedRevision {
    public final static ExpectedRevision ANY = new AnyExpectedRevision();
    public final static ExpectedRevision NO_STREAM = new NoStreamExpectedRevision();
    public final static ExpectedRevision STREAM_EXISTS = new StreamExistsExpectedRevision();
    public static ExpectedRevision expectedRevision(long revision) {
        return new SpecificExpectedRevision(revision);
    }

    abstract public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options);

    static class NoStreamExpectedRevision extends ExpectedRevision {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setNoStream(Shared.Empty.getDefaultInstance());
        }
    }

    static class AnyExpectedRevision extends ExpectedRevision {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setAny(Shared.Empty.getDefaultInstance());
        }
    }

    static class StreamExistsExpectedRevision extends ExpectedRevision {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setStreamExists(Shared.Empty.getDefaultInstance());
        }
    }

    static class SpecificExpectedRevision extends ExpectedRevision {
        final long version;

        SpecificExpectedRevision(long version) {
            this.version = version;
        }

        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setRevision(version);
        }
    }
}

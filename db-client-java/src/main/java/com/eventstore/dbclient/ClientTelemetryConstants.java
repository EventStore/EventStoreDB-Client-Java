package com.eventstore.dbclient;

public class ClientTelemetryConstants {
    public static final String INSTRUMENTATION_NAME = "eventstoredb";

    public static class Metadata {
        public static final String TRACE_ID = "$traceId";
        public static final String SPAN_ID = "$spanId";
    }

    public static class Operations {
        public static final String APPEND = "streams.append";
        public static final String SUBSCRIBE = "streams.subscribe";
    }
}

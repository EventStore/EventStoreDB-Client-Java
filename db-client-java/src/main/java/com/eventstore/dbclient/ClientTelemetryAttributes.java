package com.eventstore.dbclient;

import io.opentelemetry.semconv.ExceptionAttributes;
import io.opentelemetry.semconv.OtelAttributes;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.incubating.DbIncubatingAttributes;

public class ClientTelemetryAttributes {
    public static class Database {
        public static final String USER = DbIncubatingAttributes.DB_USER.getKey();
        public static final String SYSTEM = DbIncubatingAttributes.DB_SYSTEM.getKey();
        public static final String OPERATION = DbIncubatingAttributes.DB_OPERATION.getKey();
    }

    public static class Server {
        public static final String ADDRESS = ServerAttributes.SERVER_ADDRESS.getKey();
        public static final String PORT = ServerAttributes.SERVER_PORT.getKey();
    }

    public static class Exceptions {
        public static final String TYPE = ExceptionAttributes.EXCEPTION_TYPE.getKey();
        public static final String MESSAGE = ExceptionAttributes.EXCEPTION_MESSAGE.getKey();
        public static final String STACK_TRACE = ExceptionAttributes.EXCEPTION_STACKTRACE.getKey();
    }

    public static class EventStore {
        public static final String STREAM = "db.eventstoredb.stream";
        public static final String SUBSCRIPTION_ID = "db.eventstoredb.subscription.id";
        public static final String EVENT_ID = "db.eventstoredb.event.id";
        public static final String EVENT_TYPE = "db.eventstoredb.event.type";
    }
}

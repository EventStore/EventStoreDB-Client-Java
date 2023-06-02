package com.eventstore.dbclient;

import io.grpc.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testcontainers.module.ESDBTests;

import java.util.concurrent.atomic.AtomicInteger;

public class InterceptorTests extends ESDBTests {
    @Test
    public void testInterceptorIsCalled() {
        EventStoreDBClientSettings settings = getEmptyServer().getSettings();

        AtomicInteger atom = new AtomicInteger(0);

        settings.getInterceptors().add(new MyInterceptor(atom));

        EventStoreDBClient client = EventStoreDBClient.create(settings);
        try {
            client.readStream("foobar", ReadStreamOptions.get()).get();
        } catch (Exception e) {
            // We don't care.
        }

        Assertions.assertEquals(42, atom.get());
    }

    class MyInterceptor implements ClientInterceptor {
        final AtomicInteger atom;

        MyInterceptor(AtomicInteger atom) {
            this.atom = atom;
        }
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
            atom.set(42);
            return next.newCall(method, callOptions);
        }
    }
}

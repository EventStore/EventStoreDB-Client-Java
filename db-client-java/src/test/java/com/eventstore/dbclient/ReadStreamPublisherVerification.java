package com.eventstore.dbclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.TestEnvironment;
import testcontainers.module.EventStoreTestDBContainer;

public class ReadStreamPublisherVerification extends ReactiveStreamsPublisherVerificationTCK<ResolvedEvent> {

    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false);

    private EventStoreDBClient client;

    public ReadStreamPublisherVerification() {
        super(new TestEnvironment(2000, 500, true));
    }

    @Before
    public void setUp() {
        client = server.getClient();
    }

    @After
    public void shutdown() throws Exception {
        client.shutdown();
    }

    @Override
    public Publisher<ResolvedEvent> createPublisher(long elements) {

        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos();

        return client.readStreamReactive("dataset20M-1800", elements, options);
    }

    @Override
    public Publisher<ResolvedEvent> createFailedPublisher() {

        return client.readStreamReactive("unknown-stream");
    }
}

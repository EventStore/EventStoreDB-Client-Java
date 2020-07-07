package testcontainers.module;

import org.junit.rules.ExternalResource;

public class EventStoreStreamsClient extends ExternalResource {
    private final EventStoreTestDBContainer server;
    public com.eventstore.dbclient.StreamsClient instance;

    public EventStoreStreamsClient(EventStoreTestDBContainer server) {
        this.server = server;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        this.instance = this.server.getStreamsClient();
    }

    @Override
    protected void after() {
        super.after();

        try {
            this.instance.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

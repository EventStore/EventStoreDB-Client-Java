package testcontainers.module;

import org.junit.rules.ExternalResource;

public class EventStoreStreamsClient extends ExternalResource {
    private final EventStoreTestDBContainer server;

    public EventStoreStreamsClient(EventStoreTestDBContainer server) {
        this.server = server;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
    }

    @Override
    protected void after() {
        super.after();

        try {
            this.server.getConnectionNew().shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

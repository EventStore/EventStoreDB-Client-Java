package extensions;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import testcontainers.module.EventStoreDB;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BeforeEverythingElseExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    private static boolean started = false;
    private static EventStoreDB EMPTY_SERVER;
    private static EventStoreDB POPULATED_SERVER;
    private static EventStoreDB SECURE_EMPTY_SERVER;

    final static Lock lock = new ReentrantLock();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        lock.lock();
        if (!started) {
            started = true;
            EMPTY_SERVER = new EventStoreDB(true, true);
            SECURE_EMPTY_SERVER = new EventStoreDB(true, false);
            POPULATED_SERVER = new EventStoreDB(false, true);
            // Your "before all tests" startup logic goes here
            // The following line registers a callback hook when the root test context is
            // shut down

            // do your work - which might take some time -
            // or just uses more time than the simple check of a boolean
        }
        // free the access
        lock.unlock();
    }

    @Override
    public void close() throws Throwable {
        EMPTY_SERVER.shutdownClients();
        POPULATED_SERVER.shutdownClients();
        SECURE_EMPTY_SERVER.shutdownClients();
        EMPTY_SERVER.stop();
        POPULATED_SERVER.stop();
        SECURE_EMPTY_SERVER.stop();
    }

    public static EventStoreDB GetEmptyServer() {
        return EMPTY_SERVER;
    }

    public static EventStoreDB GetPopulatedServer() {
        return POPULATED_SERVER;
    }

    public static EventStoreDB GetSecureServer() {
        return SECURE_EMPTY_SERVER;
    }
}

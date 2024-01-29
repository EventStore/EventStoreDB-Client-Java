package com.eventstore.dbclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientTracker {
    private static final Logger logger = LoggerFactory.getLogger(ClientTracker.class);
    private ArrayList<EventStoreDBClientBase> otherClients = new ArrayList<>();
    private EventStoreDBClient defaultClient = null;

    public synchronized EventStoreDBClient createClient(EventStoreDBClientSettings settings) {
        EventStoreDBClient client = EventStoreDBClient.create(settings);
        otherClients.add(client);
        return client;
    }

    public synchronized EventStoreDBClient getDefaultClient(Database database) {
        if (defaultClient == null) {
            EventStoreDBClientSettings settings = database.defaultSettingsBuilder().buildConnectionSettings();
            defaultClient = EventStoreDBClient.create(settings);

            if (settings.isTls() && settings.getDefaultCredentials() != null) {
                for (int count = 0; count < 50; count++) {
                    logger.debug(String.format("Checking if admin user is available...%d/50", count));
                    try {
                        defaultClient.readStream("$users", ReadStreamOptions.get()).get(1, TimeUnit.SECONDS);
                        logger.debug("Admin account is available!");
                        break;
                    } catch (InterruptedException | TimeoutException e) {
                        if (e instanceof TimeoutException) {
                            logger.debug("Request timed out, retrying...");
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else
                            throw new RuntimeException(e);

                    } catch (ExecutionException e) {
                        if (e.getCause() instanceof StatusRuntimeException) {
                            StatusRuntimeException grpcExc = (StatusRuntimeException) e.getCause();
                            Status.Code code = grpcExc.getStatus().getCode();

                            if (code == Status.Code.PERMISSION_DENIED
                                    || code == Status.Code.UNAUTHENTICATED
                                    || code == Status.Code.DEADLINE_EXCEEDED
                                    || code == Status.Code.NOT_FOUND
                                    || code == Status.Code.UNAVAILABLE) {
                                logger.debug("Admin not available, retrying", grpcExc);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                                continue;
                            }
                        }

                        if (e.getCause() instanceof NotLeaderException) {
                            logger.debug("Can't access admin because of no leader exception error, retrying...");
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            continue;
                        }

                        // In some rare occasions, it's possible for GHA to take much more time setting up a cluster
                        // through docker compose. In this case, we recreate a fresh client in the case we exhausted
                        // all discovery attempts and the connection got closed.
                        if (e.getCause() instanceof ConnectionShutdownException && (settings.isDnsDiscover() || settings.getHosts().length > 1)) {
                            logger.debug("Seems we exhausted all discovery attempts. Unusual but maybe docker is slow");
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            defaultClient = EventStoreDBClient.create(settings);
                            continue;
                        }

                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return defaultClient;
    }

    public void dispose() {
        if (defaultClient != null) {
            try {
                defaultClient.shutdown().get();
            } catch (ExecutionException | InterruptedException e) {
                logger.error("Error when shutting down client", e);
            }
            defaultClient = null;
        }

        for (EventStoreDBClientBase client: otherClients) {
            try {
                client.shutdown().get();
            } catch (ExecutionException | InterruptedException e) {
                logger.error("Error when shutting down client", e);
            }
        }

        otherClients = new ArrayList<>();
    }
}

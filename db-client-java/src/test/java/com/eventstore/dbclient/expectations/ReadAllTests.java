package com.eventstore.dbclient.expectations;

import com.eventstore.dbclient.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ReadAllTests extends Expectations {
    @Test
    default void testReadAllEventsForwardFromZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getDefaultClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .notResolveLinkTos()
                .maxCount(10);

        ReadResult result = client.readAll(options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-e0-e10");
    }

    @Test
    default void testReadAllEventsForwardFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getDefaultClient();

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromPosition(new Position(1788, 1788))
                .notResolveLinkTos()
                .maxCount(10);

        ReadResult result = client.readAll(options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-c1788-p1788");
    }

    @Test
    default void testReadAllEventsBackwardsFromNonZeroPosition() throws ExecutionException, InterruptedException {
        EventStoreDBClient client = getDefaultClient();

        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromPosition(new Position(3386, 3386))
                .notResolveLinkTos()
                .maxCount(10);

        ReadResult result = client.readAll(options)
                .get();

        verifyAgainstTestData(result.getEvents(), "all-back-c3386-p3386");
    }
}

package com.eventstore.dbclient.expectations;

import com.eventstore.dbclient.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public interface ProjectionManagementTests extends ConnectionAware {
    String COUNT_EVENTS_PROJECTION_FILENAME = "count-events-projection.js";
    String COUNT_EVENTS_PARTITIONED_PROJECTION_FILENAME = "count-events-partitioned-projection.js";
    String UNKNOWN_KEYNAMES_PROJECTION_FILENAME = "state-with-unknown-keynames.js";

    int EXPECTED_EVENT_COUNT = 2000;
    Logger logger = LoggerFactory.getLogger(ProjectionManagementTests.class);

    String COUNT_EVENTS_PROJECTION = loadResourceAsString(COUNT_EVENTS_PROJECTION_FILENAME);
    String COUNT_EVENTS_PARTITIONED_PROJECTION = loadResourceAsString(COUNT_EVENTS_PARTITIONED_PROJECTION_FILENAME);
    String UNKNOWN_KEYNAMES_PROJECTION = loadResourceAsString(UNKNOWN_KEYNAMES_PROJECTION_FILENAME);

    static String loadResourceAsString(String fileName) {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ProjectionManagementTests.class.getClassLoader()
                .getResourceAsStream(fileName))))) {

            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    default void testCreateAndGetContinuousProjection() throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        String name = generateName();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        CountResult result = projectionClient
            .getResult(name, CountResult.class)
            .get();

        assertCountingProjectionResultAsExpected(result);
    }

    @Test
    @Order(2)
    default void testDeserializingBasedOnJavaType() throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        String name = generateName();
        projectionClient
            .create(name, UNKNOWN_KEYNAMES_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        Map<String, Item> result = getResultOfUnknownKeyNamesProjection(name);

        assertDeserializedIntoMap(result);
    }

    @Test
    @Order(3)
    default void testEnablingProjection() throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        String name = generateName();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        projectionClient
            .enable(name).get();

        waitUntilProjectionStatusIs(name, "Running");
    }

    @Test
    @Order(4)
    default void testDisablingProjection() throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        String name = generateName();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        projectionClient
            .enable(name).get();

        waitUntilProjectionStatusIs(name, "Running");

        projectionClient
            .disable(name).get();

        waitUntilProjectionStatusIs(name, "Stopped");
    }

    @Test
    @Order(5)
    default void testAbortingProjection() throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        String name = generateName();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        projectionClient.abort(name).get();

        waitUntilProjectionStatusIs(name, "Aborted", "Stopped");
    }

    @Test
    @Order(6)
    default void testResettingProjection() throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        String name = generateName();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        CountResult before = projectionClient
                .getResult(name, CountResult.class)
                .get();

        // stop counting
        projectionClient.disable(name).get();
        projectionClient.reset(name).get();

        CountResult after = projectionClient
                .getResult(name, CountResult.class)
                .get();

        Assertions.assertTrue(before.getCount() > 0);
        Assertions.assertEquals(0, after.getCount());
    }


    @Test
    @Order(7)
    default void testDeletingProjection() throws ExecutionException, InterruptedException, TimeoutException {
        String name = generateName();
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        projectionClient.disable(name).get();

        waitUntilProjectionStatusIs(name, "Stopped");

        CompletableFuture.runAsync(() -> {
            int count = 1;
            for (;;) {
                try {
                    projectionClient.delete(name).get();
                    break;
                } catch (Exception e) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        logger.error(String.format("Exception when deleting projection '%s', count %d", name, count), e);
                        count++;
                    }
                }
            }
        }).get(60, TimeUnit.SECONDS);
    }

    @Test
    @Order(8)
    default void testGetProjectionStatistics() throws Exception {
        Exceptions exceptions = new Exceptions().registerUnknownError();
        String name = generateName();
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();

        flaky(10, exceptions, () -> projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get());

        waitUntilProjectionStatusIs(name, "Running");

        ProjectionDetails statistics = projectionClient.getStatistics(name).get();

        Assertions.assertNotNull(statistics);
        Assertions.assertEquals(name, statistics.getName());
        Assertions.assertEquals(name, statistics.getEffectiveName());
        Assertions.assertEquals("Running", statistics.getStatus());
        Assertions.assertEquals("Continuous", statistics.getMode());
        Assertions.assertNotNull(statistics.getPosition());
        Assertions.assertNotNull(statistics.getLastCheckpoint());
    }

    @Test
    @Order(9)
    default void testUpdateProjection() throws ExecutionException, InterruptedException {
        String name = generateName();
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        UpdateProjectionOptions options = UpdateProjectionOptions.get().emitEnabled(true);

        projectionClient.update(name, COUNT_EVENTS_PARTITIONED_PROJECTION, options).get();

        ProjectionDetails details = projectionClient.getStatus(name).get();

        Assertions.assertEquals(details.getVersion(), 1);
    }

    @Test
    @Order(10)
    default void testGetProjectionState() throws ExecutionException, InterruptedException {
        String name = generateName();
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        CountResult state = projectionClient
            .getState(name, CountResult.class)
            .get();

        assertCountingProjectionResultAsExpected(state);
    }

    @Test
    @Order(11)
    default void testGetProjectionResultByPartition() throws ExecutionException, InterruptedException {
        String name = generateName();
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        projectionClient
            .create(name, COUNT_EVENTS_PARTITIONED_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        Thread.sleep(5000);

        GetProjectionResultOptions options = GetProjectionResultOptions.get()
                .partition("odd");

        CountResult oddState = projectionClient
            .getResult(name, CountResult.class, options)
            .get();

        GetProjectionStateOptions stateOptions = GetProjectionStateOptions.get()
                .partition("even");

        CountResult evenState = projectionClient
            .getState(name, CountResult.class, stateOptions)
            .get();

        assertCountingProjectionResultAsExpected(oddState);
        assertCountingProjectionResultAsExpected(evenState);

        CountResult invalidState = projectionClient
            .getState(name, CountResult.class,
                    GetProjectionStateOptions.get()
                    .partition("non-existing-partition"))
            .get();

        Assertions.assertEquals(0, invalidState.count);
    }

    @Test
    @Order(12)
    default void testGetProjectionStateByPartition() throws ExecutionException, InterruptedException {
        String name = generateName();
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        projectionClient
            .create(name, COUNT_EVENTS_PARTITIONED_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        GetProjectionStateOptions options = GetProjectionStateOptions.get()
                .partition("odd");

        CountResult oddState = projectionClient
            .getState(name, CountResult.class, options)
            .get();

        options.partition("even");

        CountResult evenState = projectionClient
            .getState(name, CountResult.class, options)
            .get();

        assertCountingProjectionResultAsExpected(oddState);
        assertCountingProjectionResultAsExpected(evenState);

        options.partition("non-existing-partition");

        CountResult invalidState = projectionClient
            .getState(name, CountResult.class, options)
            .get();

        Assertions.assertEquals(0, invalidState.count);
    }

    @Test
    @Order(13)
    default void testGetProjectionStatus() throws ExecutionException, InterruptedException {
        String name = generateName();
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        projectionClient
            .create(name, COUNT_EVENTS_PROJECTION)
            .get();

        waitUntilProjectionStatusIs(name, "Running");

        ProjectionDetails status = projectionClient
            .getStatus(name)
            .get();

        Assertions.assertNotNull(status);
        Assertions.assertEquals(name, status.getName());
        Assertions.assertEquals(name, status.getEffectiveName());
        Assertions.assertEquals("Running", status.getStatus());
        Assertions.assertEquals("Continuous", status.getMode());
        Assertions.assertNotNull(status.getPosition());
        Assertions.assertNotNull(status.getLastCheckpoint());
    }

    @Test
    @Order(14)
    default void testListProjections() throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        List<ProjectionDetails> projections = projectionClient.list().get();
        Assertions.assertTrue(projections.size() > 1);
    }

    default void assertDeserializedIntoMap(final Map<String, Item> result) {

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.entrySet().isEmpty());
        Map.Entry<String, Item> firstEntry = result.entrySet().stream().findFirst().get();
        Assertions.assertNotNull(firstEntry.getKey());
        Item firstItem = firstEntry.getValue();
        Assertions.assertNotNull(firstItem);
        Assertions.assertNotNull(firstItem.getTimeArrivedMillis());
    }

    @Test
    @Order(15)
    default void testRestartingProjectionSubsystem() throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        projectionClient.restartSubsystem().get();
    }

    default Map<String, Item> getResultOfUnknownKeyNamesProjection(String name) throws ExecutionException, InterruptedException {
        EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();

        return projectionClient
            .<Map<String, Item>>getResult(name, factory -> factory.constructMapType(HashMap.class, String.class, Item.class))
            .get();
    }

    default void assertCountingProjectionResultAsExpected(final CountResult result) {

        Assertions.assertNotNull(result);
        //The projection may not have completed so may not yet equal EXPECTED_EVENT_COUNT
        //that's okay we're not testing the server, just that the projection has been
        //created correctly and is running
        Assertions.assertTrue(result.getCount() > 0);
        Assertions.assertTrue(result.getCount() <= EXPECTED_EVENT_COUNT);
    }

    public static class CountResult {

        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(final int count) {
            this.count = count;
        }
    }

    public static class Item {

        private Long timeArrivedMillis;

        public Long getTimeArrivedMillis() {
            return timeArrivedMillis;
        }

        public void setTimeArrivedMillis(final Long timeArrivedMillis) {
            this.timeArrivedMillis = timeArrivedMillis;
        }
    }

    default void waitUntilProjectionStatusIs(String name, String... statuses) throws ExecutionException, InterruptedException {
        final EventStoreDBProjectionManagementClient projectionClient = getDefaultProjectionClient();
        CompletableFuture.runAsync(() -> {
            String last = "";
            for (int i = 0; i < 6; i++) {
                try {
                    ProjectionDetails details = projectionClient.getStatus(name).get(5, TimeUnit.SECONDS);

                    for (String status : statuses) {
                        if (details.getStatus().contains(status)) {
                            return;
                        }
                    }

                    last = details.getStatus();
                    Thread.sleep(100);
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    if (e instanceof ExecutionException) {
                        throw new RuntimeException(e);
                    }
                }
            }

            throw new RuntimeException("Projection '" + name + "' doesn't reach the expected status. Got " + last + ", Expected " + Arrays.toString(statuses));
        }).get();
    }
}

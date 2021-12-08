package com.eventstore.dbclient;

import org.junit.*;
import testcontainers.module.EventStoreTestDBContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ProjectionManagementTests {

    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false, true);

    private static final String PROJECTION_NAME = "projection";

    private static final String COUNT_EVENTS_PROJECTION_FILENAME = "count-events-projection.js";
    private static final String COUNT_EVENTS_PARTITIONED_PROJECTION_FILENAME = "count-events-partitioned-projection.js";
    private static final String UNKNOWN_KEYNAMES_PROJECTION_FILENAME = "state-with-unknown-keynames.js";

    private static final int EXPECTED_EVENT_COUNT = 2000;

    private static String COUNT_EVENTS_PROJECTION;
    private static String COUNT_EVENTS_PARTITIONED_PROJECTION;
    private static String UNKNOWN_KEYNAMES_PROJECTION;
    private static final String EMPTY_PROJECTION = "{}";

    private EventStoreDBProjectionManagementClient projectionClient;

    @BeforeClass
    public static void loadProjectionJs() throws IOException {

        COUNT_EVENTS_PROJECTION = loadResourceAsString(COUNT_EVENTS_PROJECTION_FILENAME);
        COUNT_EVENTS_PARTITIONED_PROJECTION = loadResourceAsString(COUNT_EVENTS_PARTITIONED_PROJECTION_FILENAME);
        UNKNOWN_KEYNAMES_PROJECTION = loadResourceAsString(UNKNOWN_KEYNAMES_PROJECTION_FILENAME);
    }

    private static String loadResourceAsString(String fileName) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ProjectionManagementTests.class.getClassLoader()
                .getResourceAsStream(fileName)))) {

            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    @Before
    public void init() {
        server.waitForInitialization();
        projectionClient = server.getProjectionManagementClient();
    }

    @After
    public void teardown() throws InterruptedException {
        if (projectionClient == null) {
            return;
        }
        try {
            projectionClient.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateAndGetContinuousProjection() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        CountResult result = server.getProjectionManagementClient()
            .getResult(PROJECTION_NAME, CountResult.class)
            .get();

        assertCountingProjectionResultAsExpected(result);
    }

    @Test
    public void testDeserializingBasedOnJavaType() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, UNKNOWN_KEYNAMES_PROJECTION)
            .get();

        Map<String, Item> result = getResultOfUnknownKeyNamesProjection();

        assertDeserializedIntoMap(result);
    }

    @Test
    public void testEnablingProjection() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        projectionClient
            .enable(PROJECTION_NAME).get();

        ProjectionDetails status = projectionClient
            .getStatus(PROJECTION_NAME).get();

        Assert.assertEquals("Running", status.getStatus());
    }

    @Test
    public void testDisablingProjection() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        projectionClient
            .enable(PROJECTION_NAME).get();

        projectionClient
            .disable(PROJECTION_NAME).get();

        ProjectionDetails status = projectionClient
                .getStatus(PROJECTION_NAME).get();

        Assert.assertEquals("Stopped", status.getStatus());
    }

    @Test
    public void testAbortingProjection() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        projectionClient.abort(PROJECTION_NAME).get();

        ProjectionDetails status = projectionClient
                .getStatus(PROJECTION_NAME).get();

        Assert.assertEquals("Aborted/Stopped", status.getStatus());
    }

    @Test
    public void testResettingProjection() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        CountResult before = server.getProjectionManagementClient()
                .getResult(PROJECTION_NAME, CountResult.class)
                .get();

        // stop counting
        projectionClient.disable(PROJECTION_NAME).get();
        projectionClient.reset(PROJECTION_NAME).get();

        CountResult after = server.getProjectionManagementClient()
                .getResult(PROJECTION_NAME, CountResult.class)
                .get();

        Assert.assertTrue(before.getCount() > 0);
        Assert.assertEquals(0, after.getCount());
    }

    @Test
    public void testRestartingProjectionSubsystem() throws ExecutionException, InterruptedException {

        projectionClient.restartSubsystem().get();
    }

    @Test
    public void testDeletingProjection() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        DeleteProjectionOptions options = DeleteProjectionOptions.get()
            .deleteCheckpointStream()
            .deleteStateStream()
            .keepEmittedStreams();

        projectionClient.disable(PROJECTION_NAME).get();

        projectionClient.delete(PROJECTION_NAME, options).get();

        // wait a bit for projection to be deleted
        Thread.sleep(100);

        Assert.assertThrows(ExecutionException.class, () -> {
            projectionClient.getStatus(PROJECTION_NAME).get();
        });
    }

    @Test
    public void testGetProjectionStatistics() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        ProjectionDetails statistics = projectionClient.getStatistics(PROJECTION_NAME).get();

        Assert.assertNotNull(statistics);
        Assert.assertEquals(PROJECTION_NAME, statistics.getName());
        Assert.assertEquals(PROJECTION_NAME, statistics.getEffectiveName());
        Assert.assertEquals("Running", statistics.getStatus());
        Assert.assertEquals("Continuous", statistics.getMode());
        Assert.assertNotNull(statistics.getPosition());
        Assert.assertNotNull(statistics.getLastCheckpoint());
    }

    @Test
    public void testUpdateProjection() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        UpdateProjectionOptions options = UpdateProjectionOptions.get().emitEnabled(true);

        projectionClient.update(PROJECTION_NAME, EMPTY_PROJECTION, options).get();
        projectionClient.update(PROJECTION_NAME, COUNT_EVENTS_PROJECTION).get();
        projectionClient.update(PROJECTION_NAME, EMPTY_PROJECTION).get();
    }

    @Test
    public void testGetProjectionState() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        CountResult state = projectionClient
            .getState(PROJECTION_NAME, CountResult.class)
            .get();

        assertCountingProjectionResultAsExpected(state);
    }

    @Test
    public void testGetProjectionResultByPartition() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PARTITIONED_PROJECTION)
            .get();

        CountResult oddState = projectionClient
            .getResult(PROJECTION_NAME, "odd", CountResult.class)
            .get();

        CountResult evenState = projectionClient
            .getState(PROJECTION_NAME, "even", CountResult.class)
            .get();

        assertCountingProjectionResultAsExpected(oddState);
        assertCountingProjectionResultAsExpected(evenState);

        CountResult invalidState = projectionClient
            .getState(PROJECTION_NAME, "non-existing-partition", CountResult.class)
            .get();

        Assert.assertEquals(0, invalidState.count);
    }

    @Test
    public void testGetProjectionStateByPartition() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PARTITIONED_PROJECTION)
            .get();

        CountResult oddState = projectionClient
            .getState(PROJECTION_NAME, "odd", CountResult.class)
            .get();

        CountResult evenState = projectionClient
            .getState(PROJECTION_NAME, "even", CountResult.class)
            .get();

        assertCountingProjectionResultAsExpected(oddState);
        assertCountingProjectionResultAsExpected(evenState);

        CountResult invalidState = projectionClient
            .getState(PROJECTION_NAME, "non-existing-partition", CountResult.class)
            .get();

        Assert.assertEquals(0, invalidState.count);
    }

    @Test
    public void testGetProjectionStatus() throws ExecutionException, InterruptedException {

        projectionClient
            .create(PROJECTION_NAME, COUNT_EVENTS_PROJECTION)
            .get();

        ProjectionDetails status = projectionClient
            .getStatus(PROJECTION_NAME)
            .get();

        Assert.assertNotNull(status);
        Assert.assertEquals(PROJECTION_NAME, status.getName());
        Assert.assertEquals(PROJECTION_NAME, status.getEffectiveName());
        Assert.assertEquals("Running", status.getStatus());
        Assert.assertEquals("Continuous", status.getMode());
        Assert.assertNotNull(status.getPosition());
        Assert.assertNotNull(status.getLastCheckpoint());
    }

    @Test
    public void testListProjections() throws ExecutionException, InterruptedException {

        ListProjectionsResult result = projectionClient.list().get();
        Assert.assertNotNull(result);

        List<ProjectionDetails> projections = result.getProjections();
        Assert.assertEquals(5, projections.size());

        String[] systemProjections = new String[] {
            "$by_category",
            "$by_correlation_id",
            "$by_event_type",
            "$stream_by_category",
            "$streams"
        };

        Assert.assertArrayEquals(
            Arrays.stream(systemProjections).sorted().toArray(),
            projections.stream().map(ProjectionDetails::getName).sorted().toArray());
    }

    private void assertDeserializedIntoMap(final Map<String, Item> result) {

        Assert.assertNotNull(result);
        Assert.assertFalse(result.entrySet().isEmpty());
        Map.Entry<String, Item> firstEntry = result.entrySet().stream().findFirst().get();
        Assert.assertNotNull(firstEntry.getKey());
        Item firstItem = firstEntry.getValue();
        Assert.assertNotNull(firstItem);
        Assert.assertNotNull(firstItem.getTimeArrivedMillis());
    }

    private Map<String, Item> getResultOfUnknownKeyNamesProjection() throws ExecutionException, InterruptedException {

        return server.getProjectionManagementClient()
            .<Map<String, Item>>getResult(PROJECTION_NAME, factory -> factory.constructMapType(HashMap.class, String.class, Item.class))
            .get();
    }

    private void assertCountingProjectionResultAsExpected(final CountResult result) {

        Assert.assertNotNull(result);
        //The projection may not have completed so may not yet equal EXPECTED_EVENT_COUNT
        //that's okay we're not testing the server, just that the projection has been
        //created correctly and is running
        Assert.assertTrue(result.getCount() > 0);
        Assert.assertTrue(result.getCount() <= EXPECTED_EVENT_COUNT);
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
}

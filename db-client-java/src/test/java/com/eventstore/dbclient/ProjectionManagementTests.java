package com.eventstore.dbclient;

import org.junit.*;
import testcontainers.module.EventStoreTestDBContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ProjectionManagementTests {

    @Rule
    public final EventStoreTestDBContainer server = new EventStoreTestDBContainer(false, true);

    private static final String PROJECTION_NAME = "projection";

    private static final String UNKNOWN_KEYNAMES_PROJECTION_FILENAME = "state-with-unknown-keynames.js";
    private static final String COUNT_EVENTS_PROJECTION_FILENAME = "count-events-projection.js";

    private static final int EXPECTED_EVENT_COUNT = 2000;

    private static String COUNT_EVENTS_PROJECTION;
    private static String UNKNOWN_KEYNAMES_PROJECTION;

    private EventStoreDBProjectionManagementClient eventStoreDBClient;

    @BeforeClass
    public static void loadProjectionJs() throws IOException {

        COUNT_EVENTS_PROJECTION = loadResourceAsString(COUNT_EVENTS_PROJECTION_FILENAME);
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
        eventStoreDBClient = server.getProjectionManagementClient();
    }

    @After
    public void teardown() throws InterruptedException {
        if (eventStoreDBClient == null) {
            return;
        }
        try {
            eventStoreDBClient.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateAndGetContinuousProjection() throws ExecutionException, InterruptedException {

        createProjection(COUNT_EVENTS_PROJECTION);

        CountResult result = getResultOfCountingProjection();

        assertCountingProjectionResultAsExpected(result);
    }

    @Test
    public void testDeserializingBasedOnJavaType() throws ExecutionException, InterruptedException {

        createProjection(UNKNOWN_KEYNAMES_PROJECTION);

        Map<String, Item> result = getResultOfUnknownKeyNamesProjection();

        assertDeserializedIntoMap(result);
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

    private void createProjection(
            final String projectionWithUnknownKeynames) throws InterruptedException, ExecutionException {

        eventStoreDBClient
                .createContinuous(PROJECTION_NAME, projectionWithUnknownKeynames)
                .get();
    }

    private CountResult getResultOfCountingProjection() throws InterruptedException, ExecutionException {

        return server.getProjectionManagementClient()
                .getResult(PROJECTION_NAME, CountResult.class)
                .get();
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

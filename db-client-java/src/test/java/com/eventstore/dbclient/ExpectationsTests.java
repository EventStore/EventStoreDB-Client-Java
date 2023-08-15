package com.eventstore.dbclient;

import com.eventstore.dbclient.expectations.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpectationsTests implements
        ReadStreamTests,
        ReadAllTests,
        ReadAllReactiveTests,
        SubscribeToAllTests,
        ReadStreamReactiveTests,
        SubscribeToStreamTests,
        ProjectionManagementTests {
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawnPopulatedDatabase();
        logger = LoggerFactory.getLogger(ExpectationsTests.class);
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @AfterAll
    public static void cleanup() {
        database.dispose();
    }
}

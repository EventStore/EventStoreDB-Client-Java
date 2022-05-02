package com.eventstore.dbclient;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpectedRevisionTests {

    @Test
    public void testExpectedRevisionEquality() {
        Assertions.assertEquals(ExpectedRevision.any(), new ExpectedRevision.AnyExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.noStream(), new ExpectedRevision.NoStreamExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.streamExists(), new ExpectedRevision.StreamExistsExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.expectedRevision(1L), ExpectedRevision.expectedRevision(1L));
    }

    @Test
    public void testExpectedRevisionNonEquality() {
        Assertions.assertNotEquals(ExpectedRevision.any(), ExpectedRevision.noStream());
        Assertions.assertNotEquals(ExpectedRevision.any(), ExpectedRevision.streamExists());
        Assertions.assertNotEquals(ExpectedRevision.any(), ExpectedRevision.expectedRevision(0L));
        Assertions.assertNotEquals(ExpectedRevision.noStream(), ExpectedRevision.streamExists());
        Assertions.assertNotEquals(ExpectedRevision.noStream(), ExpectedRevision.expectedRevision(0L));
        Assertions.assertNotEquals(ExpectedRevision.streamExists(), ExpectedRevision.expectedRevision(0L));
    }

    @Test
    public void testExpectedRevisionHashCode() {
        Assertions.assertEquals(ExpectedRevision.any().hashCode(), new ExpectedRevision.AnyExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.noStream().hashCode(), new ExpectedRevision.NoStreamExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.streamExists().hashCode(), new ExpectedRevision.StreamExistsExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.expectedRevision(1L).hashCode(), ExpectedRevision.expectedRevision(1L).hashCode());
    }

}

package com.eventstore.dbclient;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpectedRevisionTests {

    @Test
    public void testExpectedRevisionEquality() {
        Assertions.assertEquals(ExpectedRevision.ANY, new ExpectedRevision.AnyExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.NO_STREAM, new ExpectedRevision.NoStreamExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.STREAM_EXISTS, new ExpectedRevision.StreamExistsExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.expectedRevision(1L), ExpectedRevision.expectedRevision(1L));
    }

    @Test
    public void testExpectedRevisionNonEquality() {
        Assertions.assertNotEquals(ExpectedRevision.ANY, ExpectedRevision.NO_STREAM);
        Assertions.assertNotEquals(ExpectedRevision.ANY, ExpectedRevision.STREAM_EXISTS);
        Assertions.assertNotEquals(ExpectedRevision.ANY, ExpectedRevision.expectedRevision(0L));
        Assertions.assertNotEquals(ExpectedRevision.NO_STREAM, ExpectedRevision.STREAM_EXISTS);
        Assertions.assertNotEquals(ExpectedRevision.NO_STREAM, ExpectedRevision.expectedRevision(0L));
        Assertions.assertNotEquals(ExpectedRevision.STREAM_EXISTS, ExpectedRevision.expectedRevision(0L));
    }

    @Test
    public void testExpectedRevisionHashCode() {
        Assertions.assertEquals(ExpectedRevision.ANY.hashCode(), new ExpectedRevision.AnyExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.NO_STREAM.hashCode(), new ExpectedRevision.NoStreamExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.STREAM_EXISTS.hashCode(), new ExpectedRevision.StreamExistsExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.expectedRevision(1L).hashCode(), ExpectedRevision.expectedRevision(1L).hashCode());
    }

}

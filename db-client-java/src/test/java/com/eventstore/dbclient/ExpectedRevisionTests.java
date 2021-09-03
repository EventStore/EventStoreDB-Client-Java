package com.eventstore.dbclient;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ExpectedRevisionTests {

    @Test
    public void testExpectedRevisionEquality() {
        assertEquals(ExpectedRevision.ANY, new ExpectedRevision.AnyExpectedRevision());
        assertEquals(ExpectedRevision.NO_STREAM, new ExpectedRevision.NoStreamExpectedRevision());
        assertEquals(ExpectedRevision.STREAM_EXISTS, new ExpectedRevision.StreamExistsExpectedRevision());
        assertEquals(ExpectedRevision.expectedRevision(1L), ExpectedRevision.expectedRevision(1L));
    }

    @Test
    public void testExpectedRevisionNonEquality() {
        assertNotEquals(ExpectedRevision.ANY, ExpectedRevision.NO_STREAM);
        assertNotEquals(ExpectedRevision.ANY, ExpectedRevision.STREAM_EXISTS);
        assertNotEquals(ExpectedRevision.ANY, ExpectedRevision.expectedRevision(0L));
        assertNotEquals(ExpectedRevision.NO_STREAM, ExpectedRevision.STREAM_EXISTS);
        assertNotEquals(ExpectedRevision.NO_STREAM, ExpectedRevision.expectedRevision(0L));
        assertNotEquals(ExpectedRevision.STREAM_EXISTS, ExpectedRevision.expectedRevision(0L));
    }

    @Test
    public void testExpectedRevisionHashCode() {
        assertEquals(ExpectedRevision.ANY.hashCode(), new ExpectedRevision.AnyExpectedRevision().hashCode());
        assertEquals(ExpectedRevision.NO_STREAM.hashCode(), new ExpectedRevision.NoStreamExpectedRevision().hashCode());
        assertEquals(ExpectedRevision.STREAM_EXISTS.hashCode(), new ExpectedRevision.StreamExistsExpectedRevision().hashCode());
        assertEquals(ExpectedRevision.expectedRevision(1L).hashCode(), ExpectedRevision.expectedRevision(1L).hashCode());
    }

}

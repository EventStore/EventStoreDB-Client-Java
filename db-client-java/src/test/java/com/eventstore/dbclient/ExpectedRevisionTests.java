package com.eventstore.dbclient;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExpectedRevisionTests {

    @Test
    public void testSpecificExpectedRevisionEquality() {
        assertEquals(ExpectedRevision.expectedRevision(1L), ExpectedRevision.expectedRevision(1L));
    }

    @Test
    public void testSpecificExpectedRevisionHashCode() {
        assertEquals(ExpectedRevision.expectedRevision(1L).hashCode(), ExpectedRevision.expectedRevision(1L).hashCode());
    }

}

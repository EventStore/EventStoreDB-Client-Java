package com.eventstore.dbclient;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamRevisionTests {
    static final String largeUnsigned = "9223372036854776000";

    @Test
    public void testStreamRevisionToStringIsUnsigned() {
        assertEquals(StreamRevision.START.toString(), "0");
        assertEquals(StreamRevision.END.toString(), "18446744073709551615");

        StreamRevision revision = new StreamRevision(largeUnsigned);
        assertEquals(revision.toString(), largeUnsigned);
        assertEquals(Long.toUnsignedString(revision.getValueUnsigned()), largeUnsigned);
    }

    @Test
    public void testStreamRevisionEquality() {
        assertEquals(StreamRevision.START, new StreamRevision(0));
        assertEquals(StreamRevision.END, new StreamRevision(-1));

        StreamRevision revision1 = new StreamRevision(largeUnsigned);
        StreamRevision revision2 = new StreamRevision(Long.parseUnsignedLong(largeUnsigned));

        assertEquals(revision1, revision2);
    }

    @Test
    public void testStreamRevisionComparable() {
        assertTrue(StreamRevision.START.compareTo(StreamRevision.END) < 0);
        assertTrue(StreamRevision.END.compareTo(StreamRevision.START) > 0);

        StreamRevision revision = new StreamRevision(largeUnsigned);
        assertTrue(StreamRevision.START.compareTo(revision) < 0);
        assertTrue(StreamRevision.END.compareTo(revision) > 0);
        assertTrue(revision.compareTo(StreamRevision.START) > 0);
        assertTrue(revision.compareTo(StreamRevision.END) < 0);
    }
}

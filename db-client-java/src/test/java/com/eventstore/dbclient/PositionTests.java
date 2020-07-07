package com.eventstore.dbclient;

import org.junit.Test;

import static org.junit.Assert.*;

public class PositionTests {
    static final String largeUnsignedGreater = "9223372036854776000";
    static final String largeUnsignedLess = "9223372036854775000";

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionWithCommitLessThanPrepareLong() {
        new Position(1000, 12345);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionWithCommitLessThanPrepareString() {
        new Position("12345", "1000");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionWithCommitLessThanPrepareLargeUnsigned() {
        new Position(largeUnsignedGreater, largeUnsignedLess);
    }

    @Test
    public void testPositionEquality() {
        assertEquals(Position.START, Position.START);
        assertEquals(Position.END, Position.END);

        assertEquals(new Position(12345, 10000),
                new Position(12345, 10000));

        assertEquals(new Position(largeUnsignedLess, largeUnsignedGreater),
                new Position(largeUnsignedLess, largeUnsignedGreater));

        assertNotEquals(Position.START, Position.END);
    }

    @Test
    public void testPositionComparable() {
        assertTrue(Position.START.compareTo(Position.END) < 0);
        assertTrue(Position.END.compareTo(Position.START) > 0);

        Position position1 = new Position(largeUnsignedLess, largeUnsignedGreater);
        Position position2 = new Position(largeUnsignedLess, largeUnsignedGreater);
        assertEquals(0, position1.compareTo(position2));

        Position position3 = new Position(12345, 10000);
        Position position4 = new Position(12345, 11000);
        assertTrue(position3.compareTo(position4) < 0);

        // Whilst both of these should not exist in the same database, the actual
        // equality rules should still hold.
        Position position5 = new Position(12345, 10000);
        Position position6 = new Position(20000, 10000);
        assertTrue(position5.compareTo(position6) < 0);
    }

    @Test
    public void testPositionString() {
        assertEquals("12345/10000", new Position(12345, 10000).toString());
    }
}

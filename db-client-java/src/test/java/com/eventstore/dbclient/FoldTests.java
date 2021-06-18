package com.eventstore.dbclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

class Dumb {
    long count;
    String last;

    Dumb(long count, String last) {
        this.count = count;
        this.last = last;
    }
}

public class FoldTests {
    final static List<String> VALUES = Arrays.asList("foo", "bar", "baz");

    @Test
    public void testCollect() {
        List<String> actual = Fold.<String>collect().fold(VALUES);

        Assert.assertEquals(VALUES, actual);
    }

    @Test
    public void testCollectMap() {
        List<Integer> actual = Fold.<String, Integer>collectMap(s -> s.length()).fold(VALUES);

        Assert.assertEquals(Arrays.asList(3, 3, 3), actual);
    }

    @Test
    public void testFirst() {
        Optional<String> result = Fold.<String>first().fold(VALUES);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("foo", result.get());
    }

    @Test
    public void testLast() {
        Optional<String> result = Fold.<String>last().fold(VALUES);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("baz", result.get());
    }

    @Test
    public void testAll() {
        boolean result = Fold.<String>all(s -> s.length() == 3).fold(VALUES);

        Assert.assertTrue(result);
    }

    @Test
    public void testAllFalse() {
        boolean result = Fold.<String>all(s -> s.length() == 4).fold(VALUES);

        Assert.assertFalse(result);
    }

    @Test
    public void testAny() {
        boolean result = Fold.<String>any(s -> s.equals("bar")).fold(VALUES);

        Assert.assertTrue(result);
    }

    @Test
    public void testAnyFalse() {
        boolean result = Fold.<String>any(s -> s.equals("seev")).fold(VALUES);

        Assert.assertFalse(result);
    }

    @Test
    public void testCount() {
        long result = Fold.<String>count().fold(VALUES);

        Assert.assertEquals(3, result);
    }

    @Test
    public void testFind() {
        Optional<String> result = Fold.<String>find(s -> s.equals("bar")).fold(VALUES);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("bar", result.get());
    }

    @Test
    public void testApply() {
        Dumb result = Fold.<String>count().apply(Fold.last(), (count, last) -> new Dumb(count, last.get()))
                .fold(VALUES);

        Assert.assertEquals(result.count, 3);
        Assert.assertEquals(result.last, "baz");
    }

    @Test
    public void testValue() {
        int result = Fold.<String, Integer>value(42).fold(VALUES);

        Assert.assertEquals(42, result);
    }

    @Test
    public void testForEach() {
        final List<String> result = new ArrayList<>();

        Fold.<String>forEach(result::add).fold(VALUES);

        Assert.assertEquals(VALUES, result);
    }

    long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMap() {
        String result = Fold.<String>count().map(s -> s.toString()).fold(VALUES);

        Assert.assertEquals("3", result);
    }

    @Test
    public void testContramap() {
        Fold<Integer, Long> initial = Fold.count();
        long result = initial.<String>contramap(s -> s.length()).fold(VALUES);

        Assert.assertEquals(3, result);
    }
}

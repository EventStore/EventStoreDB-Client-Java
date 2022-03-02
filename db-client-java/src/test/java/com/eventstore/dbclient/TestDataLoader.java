package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

class TestDataLoader {
    static TestResolvedEvent[] loadSerializedResolvedEvents(String filenameStem) {
        String filename = String.format("%s.json", filenameStem);

        InputStream stream = TestDataLoader.class.getClassLoader().getResourceAsStream(filename);

        JsonMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        try {
            return mapper.readValue(stream, TestResolvedEvent[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static TestPosition[] loadSerializedPositions(String filenameStem) {
        String filename = String.format("%s.json", filenameStem);

        InputStream stream = TestDataLoader.class.getClassLoader().getResourceAsStream(filename);

        JsonMapper mapper = JsonMapper.builder().build();
        try {
            return mapper.readValue(stream, TestPosition[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static long[] loadSerializedStreamVersions(String filenameStem) {
        String filename = String.format("%s.json", filenameStem);

        InputStream stream = TestDataLoader.class.getClassLoader().getResourceAsStream(filename);

        JsonMapper mapper = JsonMapper.builder().build();
        try {
            return mapper.readValue(stream, long[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

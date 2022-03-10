package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Exception checkForError(int code) {
        switch (code) {
            case 401:
                return new RuntimeException("Access denied");
            case 404:
                return new ResourceNotFoundException();
            default:
                if (code >= 200 && code < 300)
                    return null;

                return new RuntimeException("Unexpected exception, code: " + code);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readContent(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = in.readLine()) != null) {
                content.append(line);
            }

            return content.toString();
        }
    }

    public static PersistentSubscriptionInfo parseSubscriptionInfo(JsonNode node) {
        PersistentSubscriptionInfo info = new PersistentSubscriptionInfo();

        info.setEventStreamId(node.get("eventStreamId").asText());
        info.setGroupName(node.get("groupName").asText());
        info.setStatus(node.get("status").asText());
        info.setAverageItemsPerSecond(node.get("averageItemsPerSecond").asDouble());
        info.setTotalItemsProcessed(node.get("totalItemsProcessed").asLong());
        info.setLastKnownEventNumber(node.get("lastKnownEventNumber").asLong());

        if (node.get("connectionCount") != null) {
            info.setConnectionCount(node.get("connectionCount").asLong());
        } else {
            info.setConnectionCount(0);
        }

        info.setTotalInFlightMessages(node.get("totalInFlightMessages").asLong());
        info.setConfig(parseConfig(node.get("config")));

        return info;
    }

    public static PersistentSubscriptionConfig parseConfig(JsonNode node) {
        PersistentSubscriptionConfig config = null;

        if (node != null) {
            config = new PersistentSubscriptionConfig();
            config.setResolveLinktos(node.get("resolveLinktos").asBoolean());
            config.setStartFrom(node.get("startFrom").asLong());
            config.setMessageTimeoutMilliseconds(node.get("messageTimeoutMilliseconds").asLong());
            config.setExtraStatistics(node.get("extraStatistics").asBoolean());
            config.setMaxRetryCount(node.get("maxRetryCount").asLong());
            config.setLiveBufferSize(node.get("liveBufferSize").asLong());
            config.setBufferSize(node.get("bufferSize").asLong());
            config.setReadBatchSize(node.get("readBatchSize").asLong());
            config.setPreferRoundRobin(node.get("preferRoundRobin").asBoolean());
            config.setCheckPointAfterMilliseconds(node.get("checkPointAfterMilliseconds").asLong());
            config.setMinCheckPointCount(node.get("minCheckPointCount").asLong());
            config.setMaxCheckPointCount(node.get("maxCheckPointCount").asLong());
            config.setMaxSubscriberCount(node.get("maxSubscriberCount").asLong());
            config.setNamedConsumerStrategy(ConsumerStrategy.valueOf(node.get("namedConsumerStrategy").asText()));
        }

        return config;
    }

    public static PersistentSubscriptionInfo parseInfoFromWire(Persistent.SubscriptionInfo wire) {
        PersistentSubscriptionInfo info = new PersistentSubscriptionInfo();

        info.setEventStreamId(wire.getEventSource());
        info.setGroupName(wire.getGroupName());
        info.setConnectionCount(wire.getConnectionsCount());
        info.setStatus(wire.getStatus());
        info.setAverageItemsPerSecond(wire.getAveragePerSecond());

        try {
            info.setLastKnownEventNumber(Integer.parseInt(wire.getLastKnownEventPosition()));
        } catch (NumberFormatException e) {
            // TODO - Support log position parsing.
        }

        info.setTotalInFlightMessages(wire.getTotalInFlightMessages());
        info.setTotalItemsProcessed(wire.getTotalItems());

        PersistentSubscriptionConfig config = new PersistentSubscriptionConfig();

        config.setBufferSize(wire.getBufferSize());
        config.setExtraStatistics(wire.getExtraStatistics());
        config.setNamedConsumerStrategy(ConsumerStrategy.valueOf(wire.getNamedConsumerStrategy()));
        config.setLiveBufferSize(wire.getLiveBufferSize());
        config.setMaxCheckPointCount(wire.getMaxCheckPointCount());
        config.setCheckPointAfterMilliseconds(wire.getCheckPointAfterMilliseconds());
        config.setMaxRetryCount(wire.getMaxRetryCount());
        config.setMaxSubscriberCount(wire.getMaxSubscriberCount());
        config.setMessageTimeoutMilliseconds(wire.getMessageTimeoutMilliseconds());
        config.setMinCheckPointCount(wire.getMinCheckPointCount());
        config.setPreferRoundRobin(config.getNamedConsumerStrategy() == ConsumerStrategy.RoundRobin);
        config.setReadBatchSize(config.getReadBatchSize());
        config.setResolveLinktos(config.isResolveLinktos());

        info.setConfig(config);

        return info;
    }
}

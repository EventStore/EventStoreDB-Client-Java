package com.eventstore.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.Test;
import testcontainers.module.EventStoreStreamsClient;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ClusterDiscoveryTests {
    @Test
    public void testThing() throws SSLException, ExecutionException, InterruptedException {
//        UserCredentials creds = new UserCredentials("admin", "changeit");
//
//        SslContext sslContext = GrpcSslContexts.
//                forClient().
//                trustManager(InsecureTrustManagerFactory.INSTANCE).
//                build();
//
//        ManagedChannel channel = NettyChannelBuilder.forTarget("eventstore")
//                .nameResolverFactory(new GossipResolverFactory())
//                .userAgent("Event Store Client (Java) v1.0.0-SNAPSHOT")
//                .sslContext(sslContext)
//                .build();
//
//        StreamsClient client = new StreamsClient(channel, creds, Timeouts.DEFAULT);
//
//        final String streamName = UUID.randomUUID().toString();
//        final String eventType = "TestEvent";
//        final String eventId = "38fffbc2-339e-11ea-8c7b-784f43837872";
//        final byte[] eventMetaData = new byte[]{0xd, 0xe, 0xa, 0xd};
//        final byte[] eventData = new byte[]{0xb, 0xe, 0xe, 0xf};
//
//        List<ProposedEvent> events = new ArrayList<>();
//        events.add(new ProposedEvent(UUID.fromString(eventId), eventType, "application/octet-stream", eventData, eventMetaData));
//
//        CompletableFuture<WriteResult> future = client.appendToStream(streamName, SpecialStreamRevision.NO_STREAM, events);
//        WriteResult result = future.get();
//
//        assertEquals(new StreamRevision(0), result.getNextExpectedRevision());
    }
}

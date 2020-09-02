package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;
import io.grpc.stub.ClientCallStreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class PersistentSubscription {
    private final ClientCallStreamObserver<Persistent.ReadReq> requestStream;
    private final String subscriptionId;
    private final String streamName;
    private final String groupName;
    private final int bufferSize;
    private final Persistent.ReadReq.Options.Builder options;

    public PersistentSubscription(ClientCallStreamObserver<Persistent.ReadReq> requestStream, String subscriptionId, String streamName, String groupName, int bufferSize, Persistent.ReadReq.Options.Builder options) {
        this.requestStream = requestStream;
        this.subscriptionId = subscriptionId;
        this.streamName = streamName;
        this.groupName = groupName;
        this.bufferSize = bufferSize;
        this.options = options;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void stop() {
        this.requestStream.cancel("user-initiated", null);
    }

    public void ack(ResolvedEvent ...events) {
        this.ack(Arrays.stream(events).iterator());
    }

    public void ack(Iterator<ResolvedEvent> events) {
        Persistent.ReadReq.Ack.Builder ackBuilder = Persistent.ReadReq.Ack.newBuilder()
                .setId(ByteString.copyFromUtf8(subscriptionId));

        while (events.hasNext()) {
            ResolvedEvent event = events.next();
            RecordedEvent record = event.getLink() != null ? event.getLink() : event.getEvent();
            Shared.UUID.Structured structured = Shared.UUID.Structured.newBuilder()
                    .setLeastSignificantBits(record.getEventId().getLeastSignificantBits())
                    .setMostSignificantBits(record.getEventId().getMostSignificantBits())
                    .build();

            Shared.UUID uuid = Shared.UUID.newBuilder().setStructured(structured).build();
            ackBuilder.addIds(uuid);
        }

        Persistent.ReadReq.Ack ack = ackBuilder.build();
        Persistent.ReadReq req = Persistent.ReadReq.newBuilder()
                .setAck(ack)
                .build();

        requestStream.onNext(req);
    }

    public void nack(NackAction action, String reason, ResolvedEvent...events) {
        this.nack(action, reason, Arrays.stream(events).iterator());
    }

    public void nack(NackAction action, String reason, Iterator<ResolvedEvent> events) {
        Persistent.ReadReq.Nack.Builder nackBuilder = Persistent.ReadReq.Nack.newBuilder()
                .setId(ByteString.copyFromUtf8(subscriptionId));

        while (events.hasNext()) {
            ResolvedEvent event = events.next();
            RecordedEvent record = event.getLink() != null ? event.getLink() : event.getEvent();
            Shared.UUID.Structured structured = Shared.UUID.Structured.newBuilder()
                    .setLeastSignificantBits(record.getEventId().getLeastSignificantBits())
                    .setMostSignificantBits(record.getEventId().getMostSignificantBits())
                    .build();

            Shared.UUID uuid = Shared.UUID.newBuilder().setStructured(structured).build();
            nackBuilder.addIds(uuid);
        }

        nackBuilder.setReason(reason);
        switch (action) {
            case Park:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Park);
                break;
            case Retry:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Retry);
                break;
            case Skip:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Skip);
                break;
            case Stop:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Stop);
                break;
        }

        Persistent.ReadReq req = Persistent.ReadReq.newBuilder()
                .setNack(nackBuilder)
                .build();

        requestStream.onNext(req);
    }
}

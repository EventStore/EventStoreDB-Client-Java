package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsGrpc;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractRead {
    protected static final StreamsOuterClass.ReadReq.Options.Builder defaultReadOptions;

    private final GrpcClient client;
    protected final Metadata metadata;

    protected AbstractRead(GrpcClient client, Metadata metadata) {
        this.client = client;
        this.metadata = metadata;
    }

    static {
        defaultReadOptions = StreamsOuterClass.ReadReq.Options.newBuilder()
                .setUuidOption(StreamsOuterClass.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
    }

    public abstract StreamsOuterClass.ReadReq.Options.Builder createOptions();

    public CompletableFuture<ReadResult> execute() {
        return this.client.run(channel -> {
            StreamsOuterClass.ReadReq request = StreamsOuterClass.ReadReq.newBuilder()
                    .setOptions(createOptions())
                    .build();

            Metadata headers = this.metadata;
            StreamsGrpc.StreamsStub client = MetadataUtils.attachHeaders(StreamsGrpc.newStub(channel), headers);

            CompletableFuture<ReadResult> future = new CompletableFuture<>();
            ArrayList<ResolvedEvent> resolvedEvents = new ArrayList<>();

            client.read(request, new StreamObserver<StreamsOuterClass.ReadResp>() {
                private boolean completed = false;

                @Override
                public void onNext(StreamsOuterClass.ReadResp value) {
                    if (value.hasStreamNotFound()) {
                        future.completeExceptionally(new StreamNotFoundException());
                        this.completed = true;
                        return;
                    }

                    if (value.hasEvent()) {
                        resolvedEvents.add(ResolvedEvent.fromWire(value.getEvent()));
                    }
                }

                @Override
                public void onCompleted() {
                    if (this.completed) {
                        return;
                    }

                    future.complete(new ReadResult(resolvedEvents));
                }

                @Override
                public void onError(Throwable t) {
                    if (this.completed) {
                        return;
                    }

                    if (t instanceof StatusRuntimeException) {
                        StatusRuntimeException e = (StatusRuntimeException) t;
                        String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                        String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                        if (leaderHost != null && leaderPort != null) {
                            NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                            future.completeExceptionally(reason);
                            return;
                        }
                    }

                    future.completeExceptionally(t);
                }
            });
            return future;
        });
    }
}

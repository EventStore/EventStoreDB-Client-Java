package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsGrpc;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public abstract class AbstractRead {
    protected static final StreamsOuterClass.ReadReq.Options.Builder defaultReadOptions;

    private final Logger logger = LoggerFactory.getLogger(AbstractRead.class);
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

            LinkedBlockingQueue<ResolvedEvent> result = new LinkedBlockingQueue<>(Consts.DEFAULT_QUEUE_CAPACITY);
            client.read(request, new StreamObserver<StreamsOuterClass.ReadResp>() {
                private boolean completed = false;
                private boolean firstTime = true;

                @Override
                public void onNext(StreamsOuterClass.ReadResp value) {
                    if (this.completed)
                        return;

                    if (value.hasStreamNotFound()) {
                        future.completeExceptionally(new StreamNotFoundException());
                        this.completed = true;
                        return;
                    }

                    if (value.hasEvent()) {
                        try {
                            result.put(ResolvedEvent.fromWire(value.getEvent()));

                            if (this.firstTime) {
                                this.firstTime = false;

                                future.complete(new ReadResult(result));
                            }
                        } catch (InterruptedException e) {
                            // TODO - we might consider switching back to an observable interface considering how bad async Java story is.
                            logger.error("Exception occurred when consuming read result. Your iterator will stop producing value.", e);
                            this.completed = true;
                        }
                    }
                }

                @Override
                public void onCompleted() {
                    if (this.completed) {
                        return;
                    }
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

package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

final class GrpcUtils {
    static public <ReqT, RespT> ClientResponseObserver<ReqT, RespT> convertSingleResponse(
            CompletableFuture<RespT> dest) {

        return convertSingleResponse(dest, x -> x);
    }

    static public <ReqT, RespT, TargetT, ExceptionT extends Throwable> ClientResponseObserver<ReqT, RespT> convertSingleResponse(
            CompletableFuture<TargetT> dest, ThrowingFunction<RespT, TargetT, ExceptionT> converter) {

        return new ClientResponseObserver<ReqT, RespT>() {
            @Override
            public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
            }

            @Override
            public void onNext(RespT value) {
                try {
                    TargetT converted = converter.apply(value);
                    dest.complete(converted);
                } catch (Throwable e) {
                    dest.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusRuntimeException) {
                    StatusRuntimeException e = (StatusRuntimeException) t;
                    String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                    String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                    if (leaderHost != null && leaderPort != null) {
                        NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                        dest.completeExceptionally(reason);
                        return;
                    }
                }

                dest.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }

    static public StreamsOuterClass.ReadReq.Options.StreamOptions toStreamOptions(String streamName, StreamPosition<Long> revision) {
        StreamsOuterClass.ReadReq.Options.StreamOptions.Builder builder = StreamsOuterClass.ReadReq.Options.StreamOptions.newBuilder()
                .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                        .setStreamName(ByteString.copyFromUtf8(streamName))
                        .build());

        if (revision.isEnd()) {
            return builder.setEnd(Shared.Empty.getDefaultInstance())
                    .build();
        }

        if (revision.isStart()) {
            return builder.setStart(Shared.Empty.getDefaultInstance())
                    .build();
        }

        return builder.setRevision(revision.getPositionOrThrow())
                .build();
    }

    static public <S extends AbstractAsyncStub<S>, O> S configureStub(S stub, EventStoreDBClientSettings settings, OptionsBase<O> options) {
        S finalStub = stub;
        ConnectionMetadata metadata = new ConnectionMetadata();

        if (options.getKind() != OperationKind.Streaming) {
            long deadlineInMs = 10_000;

            if (options.getDeadline() != null) {
                deadlineInMs = options.getDeadline();
            } else if (settings.getDefaultDeadline() != null) {
                deadlineInMs = settings.getDefaultDeadline();
            }

            finalStub = finalStub.withDeadlineAfter(deadlineInMs, TimeUnit.MILLISECONDS);
        }

        UserCredentials credentials = null;

        if (options.hasUserCredentials()) {
            credentials = options.getCredentials();
        } else if (settings.getDefaultCredentials() != null) {
            credentials = settings.getDefaultCredentials().toUserCredentials();
        }

        if (credentials != null) {
            metadata.authenticated(credentials);
        }

        if (options.isLeaderRequired()) {
            metadata.requiresLeader();
        }

        return finalStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata.build()));
    }
}

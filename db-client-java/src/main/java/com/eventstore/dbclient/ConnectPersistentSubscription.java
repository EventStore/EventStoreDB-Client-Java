package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;

public class ConnectPersistentSubscription {
    private static final Persistent.ReadReq.Options.Builder defaultReadOptions;
    private final GrpcClient connection;
    private final String stream;
    private final String group;
    private final PersistentSubscriptionListener listener;
    private final ConnectPersistentSubscriptionOptions options;

    static {
        defaultReadOptions = Persistent.ReadReq.Options.newBuilder()
                .setUuidOption(Persistent.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
    }

    public ConnectPersistentSubscription(GrpcClient connection, String stream, String group, PersistentSubscriptionListener listener, ConnectPersistentSubscriptionOptions options) {
        this.connection = connection;
        this.stream = stream;
        this.group = group;
        this.listener = listener;
        this.options = options;
    }

    public CompletableFuture execute(int bufferSize) {
        return this.connection.run(channel -> {
            Metadata headers = this.options.getMetadata();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client = MetadataUtils.attachHeaders(PersistentSubscriptionsGrpc.newStub(channel), headers);

            final CompletableFuture<PersistentSubscription> result = new CompletableFuture<>();

            Shared.StreamIdentifier streamIdentifier =
                    Shared.StreamIdentifier.newBuilder()
                            .setStreamName(ByteString.copyFromUtf8(stream))
                            .build();

            Persistent.ReadReq.Options options = defaultReadOptions.clone()
                    .setBufferSize(bufferSize)
                    .setStreamIdentifier(streamIdentifier)
                    .setGroupName(group)
                    .build();

            Persistent.ReadReq req = Persistent.ReadReq.newBuilder()
                    .setOptions(options)
                    .build();

            ClientResponseObserver<Persistent.ReadReq, Persistent.ReadResp> observer = new ClientResponseObserver<Persistent.ReadReq, Persistent.ReadResp>() {
                private boolean _confirmed;
                private PersistentSubscription _subscription;
                private ClientCallStreamObserver<Persistent.ReadReq> _requestStream;

                @Override
                public void beforeStart(ClientCallStreamObserver<Persistent.ReadReq> requestStream) {
                    this._requestStream = requestStream;
                }

                @Override
                public void onNext(Persistent.ReadResp readResp) {
                    if (!_confirmed && readResp.hasSubscriptionConfirmation()) {
                        this._confirmed = true;
                        this._subscription = new PersistentSubscription(this._requestStream, readResp.getSubscriptionConfirmation().getSubscriptionId(), stream, group, bufferSize, defaultReadOptions);
                        result.complete(this._subscription);
                        return;
                    }

                    if (!_confirmed && readResp.hasEvent()) {
                        onError(new IllegalStateException("Unconfirmed persistent subscription received event"));
                        return;
                    }

                    if (_confirmed && !readResp.hasEvent()) {
                        onError(new IllegalStateException(
                                String.format("Confirmed persistent subscription %s received non-{event,checkpoint} variant",
                                        _subscription.getSubscriptionId())));
                        return;
                    }

                    listener.onEvent(this._subscription, ResolvedEvent.fromWire(readResp.getEvent()));
                }

                @Override
                public void onError(Throwable t) {
                    if (t instanceof StatusRuntimeException) {
                        Status s = ((StatusRuntimeException) t).getStatus();
                        if (s.getCode() == Status.Code.CANCELLED) {
                            listener.onCancelled(this._subscription);
                            return;
                        }
                    }

                    listener.onError(this._subscription, t);
                }

                @Override
                public void onCompleted() {
                    // Subscriptions should only complete on error.
                }
            };

            StreamObserver<Persistent.ReadReq> wireStream = client.read(observer);
            wireStream.onNext(req);

            return result;
        });
    }
}

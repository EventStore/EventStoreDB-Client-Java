package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.users.UsersGrpc;
import com.eventstore.dbclient.proto.users.UsersOuterClass;
import com.eventstore.dbclient.proto.users.UsersOuterClass.DisableReq.Options;

import java.util.concurrent.CompletableFuture;

import static com.eventstore.dbclient.GrpcUtils.configureStub;
import static com.eventstore.dbclient.GrpcUtils.convertSingleResponse;

class DisableUser {
    private final GrpcClient client;
    private final String loginName;

    public DisableUser(GrpcClient client, String loginName) {
        this.client = client;
        this.loginName = loginName;
    }

    public CompletableFuture<Void> execute() {
        return this.client.run(channel -> {
            CompletableFuture<Void> result = new CompletableFuture<>();
            UsersGrpc.UsersStub users = configureStub(UsersGrpc.newStub(channel), this.client.getSettings(), UserOptions.get());
            users.disable(request(), convertSingleResponse(result, resp -> null));
            return result;
        });
    }

    private UsersOuterClass.DisableReq request() {
        Options options = Options.newBuilder().setLoginName(loginName).build();
        return UsersOuterClass.DisableReq.newBuilder().setOptions(options).build();
    }

}

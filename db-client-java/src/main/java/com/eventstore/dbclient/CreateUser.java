package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.users.UsersGrpc;
import com.eventstore.dbclient.proto.users.UsersOuterClass;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.eventstore.dbclient.GrpcUtils.configureStub;
import static com.eventstore.dbclient.GrpcUtils.convertSingleResponse;

class CreateUser {
    private final GrpcClient client;
    private final String loginName;
    private final String fullName;
    private final String password;
    private final List<String> groups;

    public CreateUser(GrpcClient client, String loginName, String fullName, String password, List<String> groups) {
        this.client = client;
        this.loginName = loginName;
        this.fullName = fullName;
        this.password = password;
        this.groups = groups;
    }

    public CompletableFuture<Void> execute() {
        return this.client.run(channel -> {
            CompletableFuture<Void> result = new CompletableFuture<>();
            UsersGrpc.UsersStub users = configureStub(UsersGrpc.newStub(channel), this.client.getSettings(), UserOptions.get());
            users.create(request(), convertSingleResponse(result, resp -> null));
            return result;
        });
    }

    private UsersOuterClass.CreateReq request() {
        UsersOuterClass.CreateReq.Options options = UsersOuterClass.CreateReq.Options.newBuilder()
                .setLoginName(loginName)
                .setFullName(fullName)
                .addAllGroups(groups)
                .setPassword(password)
                .build();
        return UsersOuterClass.CreateReq.newBuilder().setOptions(options).build();
    }

}

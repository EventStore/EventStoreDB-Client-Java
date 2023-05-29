package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.users.UsersGrpc;
import com.eventstore.dbclient.proto.users.UsersOuterClass;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.eventstore.dbclient.GrpcUtils.configureStub;
import static com.eventstore.dbclient.GrpcUtils.convertSingleResponse;

class UpdateUser {
    private final GrpcClient client;
    private final String loginName;
    private final String fullName;
    private final String password;
    private final List<String> groups;

    public UpdateUser(GrpcClient client, String loginName, String fullName, String password, List<String> groups) {
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
            users.update(request(), convertSingleResponse(result, resp -> null));
            return result;
        });
    }

    private UsersOuterClass.UpdateReq request() {
        UsersOuterClass.UpdateReq.Options options = UsersOuterClass.UpdateReq.Options.newBuilder()
                .setLoginName(loginName)
                .setFullName(fullName)
                .addAllGroups(groups)
                .setPassword(password)
                .build();
        return UsersOuterClass.UpdateReq.newBuilder().setOptions(options).build();
    }

}

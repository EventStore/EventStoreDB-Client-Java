package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.users.UsersGrpc;
import com.eventstore.dbclient.proto.users.UsersOuterClass;
import com.eventstore.dbclient.proto.users.UsersOuterClass.ChangePasswordReq.Options;

import java.util.concurrent.CompletableFuture;

import static com.eventstore.dbclient.GrpcUtils.configureStub;
import static com.eventstore.dbclient.GrpcUtils.convertSingleResponse;

class ChangePassword {
    private final GrpcClient client;
    private final String loginName;
    private final String currentPassword;
    private final String newPassword;

    public ChangePassword(GrpcClient client, String loginName, String currentPassword, String newPassword) {
        this.client = client;
        this.loginName = loginName;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public CompletableFuture<Void> execute() {
        return this.client.run(channel -> {
            CompletableFuture<Void> result = new CompletableFuture<>();
            UsersGrpc.UsersStub users = configureStub(UsersGrpc.newStub(channel), this.client.getSettings(), UserOptions.get());
            users.changePassword(request(), convertSingleResponse(result, resp -> null));
            return result;
        });
    }

    private UsersOuterClass.ChangePasswordReq request() {
        Options options = Options.newBuilder().setLoginName(loginName)
                .setCurrentPassword(currentPassword)
                .setNewPassword(newPassword)
                .build();
        return UsersOuterClass.ChangePasswordReq.newBuilder().setOptions(options).build();
    }

}

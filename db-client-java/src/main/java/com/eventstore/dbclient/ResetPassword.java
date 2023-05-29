package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.users.UsersGrpc;
import com.eventstore.dbclient.proto.users.UsersOuterClass;
import com.eventstore.dbclient.proto.users.UsersOuterClass.ResetPasswordReq.Options;

import java.util.concurrent.CompletableFuture;

import static com.eventstore.dbclient.GrpcUtils.configureStub;
import static com.eventstore.dbclient.GrpcUtils.convertSingleResponse;

class ResetPassword {
    private final GrpcClient client;
    private final String loginName;
    private final String newPassword;

    public ResetPassword(GrpcClient client, String loginName, String newPassword) {
        this.client = client;
        this.loginName = loginName;
        this.newPassword = newPassword;
    }

    public CompletableFuture<Void> execute() {
        return this.client.run(channel -> {
            CompletableFuture<Void> result = new CompletableFuture<>();
            UsersGrpc.UsersStub users = configureStub(UsersGrpc.newStub(channel), this.client.getSettings(), UserOptions.get());
            users.resetPassword(request(), convertSingleResponse(result, resp -> null));
            return result;
        });
    }

    private UsersOuterClass.ResetPasswordReq request() {
        Options options = Options.newBuilder().setLoginName(loginName).setNewPassword(newPassword).build();
        return UsersOuterClass.ResetPasswordReq.newBuilder().setOptions(options).build();
    }

}

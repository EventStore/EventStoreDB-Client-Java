package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.users.UsersGrpc;
import com.eventstore.dbclient.proto.users.UsersOuterClass;
import com.eventstore.dbclient.proto.users.UsersOuterClass.DetailsReq.Options;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.eventstore.dbclient.GrpcUtils.configureStub;
import static com.eventstore.dbclient.GrpcUtils.convertSingleResponse;
import static java.time.Instant.ofEpochSecond;

class GetUserDetails {
    private final GrpcClient client;
    private final String loginName;

    public GetUserDetails(GrpcClient client, String loginName) {
        this.client = client;
        this.loginName = loginName;
    }

    public CompletableFuture<User> execute() {
        return this.client.run(channel -> {
            CompletableFuture<User> result = new CompletableFuture<>();
            UsersGrpc.UsersStub users = configureStub(UsersGrpc.newStub(channel), this.client.getSettings(), UserOptions.get());
            users.details(request(), convertSingleResponse(result, resp -> {
                UsersOuterClass.DetailsResp.UserDetails userDetails = resp.getUserDetails();
                UsersOuterClass.DetailsResp.UserDetails.DateTime lastUpdated = userDetails.getLastUpdated();
                return new User(
                        userDetails.getLoginName(),
                        userDetails.getFullName(),
                        userDetails.getGroupsList(),
                        fromDotNetTicks(lastUpdated.getTicksSinceEpoch()),
                        userDetails.getDisabled());
            }));
            return result;
        });
    }

    private static Instant fromDotNetTicks(long ticks) {
        return Instant.EPOCH.plusNanos(ticks * 100);
    }

    private UsersOuterClass.DetailsReq request() {
        Options options = Options.newBuilder().setLoginName(loginName).build();
        return UsersOuterClass.DetailsReq.newBuilder().setOptions(options).build();
    }

}

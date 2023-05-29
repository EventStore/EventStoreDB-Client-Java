package com.eventstore.dbclient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventStoreDBUserManagementClient extends EventStoreDBClientBase {


    protected EventStoreDBUserManagementClient(EventStoreDBClientSettings settings) {
        super(settings);
    }

    public static EventStoreDBUserManagementClient create(EventStoreDBClientSettings settings) {
        return new EventStoreDBUserManagementClient(settings);
    }

    public CompletableFuture<Void> create(String loginName, String fullName, String password, List<String> groups) {
        return new CreateUser(getGrpcClient(), loginName, fullName, password, groups).execute();
    }

    public CompletableFuture<Void> update(String loginName, String fullName, String password, List<String> groups) {
        return new UpdateUser(getGrpcClient(), loginName, fullName, password, groups).execute();
    }

    public CompletableFuture<Void> disable(String loginName) {
        return new DisableUser(getGrpcClient(), loginName).execute();
    }

    public CompletableFuture<Void> enable(String loginName) {
        return new EnableUser(getGrpcClient(), loginName).execute();
    }

    public CompletableFuture<Void> changePassword(String loginName, String currentPassword, String newPassword) {
        return new ChangePassword(getGrpcClient(), loginName, currentPassword, newPassword).execute();
    }

    public CompletableFuture<Void> resetPassword(String loginName, String newPassword) {
        return new ResetPassword(getGrpcClient(), loginName, newPassword).execute();
    }

    public CompletableFuture<Void> delete(String loginName) {
        return new DeleteUser(getGrpcClient(), loginName).execute();
    }

    public CompletableFuture<User> details(String loginName) {
        return new GetUserDetails(getGrpcClient(), loginName).execute();
    }
}

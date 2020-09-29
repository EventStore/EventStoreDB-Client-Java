package com.eventstore.dbclient;

public class PersistentSubscriptions {
    private final EventStoreDBConnection connection;
    private final UserCredentials credentials;

    private PersistentSubscriptions(EventStoreDBConnection connection, UserCredentials credentials) {
        this.connection = connection;
        this.credentials = credentials;
    }

    static public PersistentSubscriptions create(EventStoreDBConnection connection) {
        return new PersistentSubscriptions(connection, null);
    }

    static public PersistentSubscriptions createWithDefaultCredentials(EventStoreDBConnection connection, String login, String password) {
        return createWithDefaultCredentials(connection, new UserCredentials(login, password));
    }

    static public PersistentSubscriptions createWithDefaultCredentials(EventStoreDBConnection connection, UserCredentials credentials) {
        return new PersistentSubscriptions(connection, credentials);
    }

    public CreatePersistentSubscription create(String stream, String group) {
        return new CreatePersistentSubscription(this.connection, stream, group, this.credentials);
    }

    public UpdatePersistentSubscription update(String stream, String group) {
        return new UpdatePersistentSubscription(this.connection, stream, group, this.credentials);
    }

    public DeletePersistentSubscription delete(String stream, String group) {
        return new DeletePersistentSubscription(this.connection, stream, group, this.credentials);
    }

    public ConnectPersistentSubscription connect(String stream, String group, PersistentSubscriptionListener listener) {
        return new ConnectPersistentSubscription(this.connection, stream, group, this.credentials, listener);
    }
}

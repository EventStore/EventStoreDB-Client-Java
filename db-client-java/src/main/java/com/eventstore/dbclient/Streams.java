package com.eventstore.dbclient;

public class Streams {
    private EventStoreNodeConnection connection;
    private UserCredentials credentials;

    private Streams(EventStoreNodeConnection connection, UserCredentials credentials) {
        this.connection = connection;
        this.credentials = credentials;
    }

    static public Streams create(EventStoreNodeConnection connection) {
        return new Streams(connection, null);
    }

    static public Streams createWithDefaultCredentials(EventStoreNodeConnection connection, UserCredentials credentials) {
        return new Streams(connection, credentials);
    }

    static public Streams createWithDefaultCredentials(EventStoreNodeConnection connection, String login, String password) {
        return createWithDefaultCredentials(connection, new UserCredentials(login, password));
    }

    public AppendToStream appendStream(String streamName) {
        return new AppendToStream(this.connection, streamName, this.credentials);
    }

    public ReadStream readStream(String streamName) {
        return new ReadStream(this.connection, streamName, this.credentials);
    }

    public ReadAll readAll() {
        return new ReadAll(this.connection, this.credentials);
    }

    public SubscribeToStream subscribeToStream(String streamName, SubscriptionListener listener) {
        return new SubscribeToStream(this.connection, streamName, listener, this.credentials);
    }

    public SubscribeToAll subscribeToAll(SubscriptionListener listener) {
        return new SubscribeToAll(this.connection, listener, this.credentials);
    }

    public DeleteStream deleteStream(String streamName) {
        return new DeleteStream(this.connection, streamName, this.credentials);
    }
}

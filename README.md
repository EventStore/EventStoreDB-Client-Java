# EventStoreDB Client SDK for Java

This repository contains an [EventStoreDB][es] Client SDK written in Java for use with languages on the JVM. It is
compatible with Java 8 and above.

Note: This client is currently under active development and further API changes are expected. Feedback is very welcome.

## Developing

The SDK is built using [`Gradle`][gradle]. Integration tests run against a server using Docker, with the [EventStoreDB gRPC
Client Test Container][container]. Packages are not currently published to Maven Central, but will be once this library
approaches release.

## EventStoreDB Server Compatibility

This client is compatible with version `20.6.1` upwards.

Server setup instructions can be found in the [docs], follow the docker setup for the simplest configuration.

## Example

The following snippet showcases a simple example where we form a connection, then write and read events from the server.

```java
class AccountCreated {
    private UUID id;
    private String login;

    public UUID getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
```
```java
import com.eventstore.dbclient.Connections;
import com.eventstore.dbclient.EventStoreDBConnection;
import com.eventstore.dbclient.ProposedEvent;
import com.eventstore.dbclient.WriteResult;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.Streams;

public class Main {
    public static void main(String args[]) {
        EventStoreDBConnection connection = Connections.builder()
                .createSingleNodeConnection("localhost", 2113);

        Streams streamsAPI = Streams.create(connection);

        AccountCreated createdEvent = new AccountCreated();

        createdEvent.setId(UUID.randomUUID());
        createdEvent.setLogin("ouros");

        ProposedEvent event = ProposedEvent
                .builderAsJson("account-created", createdEvent)
                .build();

        WriteResult writeResult = streamsAPI.appendStream("accounts")
                .addEvent(event)
                .execute()
                .get();

        ResolvedEvent resolvedEvent = streamsAPI.readStream("accounts")
                .fromStart()
                .execute(1)
                .get()
                .getEvents()
                .get(0);

        AccountCreated writtenEvent = resolvedEvent.getOriginalEvent()
                .getEventDataAs(AccountCreated.class);
    }
}
```

## Support

Information on support can be found on our website: [Event Store Support][support]

## Documentation

Documentation for EventStoreDB can be found in the [docs].

Bear in mind that this client is not yet properly documented. We are working hard on a new version of the documentation.

## Community

We have a community discussion space at [Event Store Discuss][discuss].

## Contributing

All contributions to the SDK are made via GitHub Pull Requests, and must be licensed under the Apache 2.0 license. Please
review our [Contributing Guide][contributing] and [Code of Conduct][code-of-conduct] for more information.

[es]: https://eventstore.com
[gradle]: https://gradle.org
[container]: https://github.com/EventStore/EventStore-Client-gRPC-TestData
[contributing]: https://github.com/EventStore/EventStoreDB-Client-Java/tree/master/CONTRIBUTING.md
[code-of-conduct]: https://github.com/EventStore/EventStoreDB-Client-Java/tree/master/CODE-OF-CONDUCT.md
[support]: https://eventstore.com/support/
[docs]: https://developers.eventstore.com/server/20.6/server/installation/
[discuss]: https://discuss.eventstore.com/

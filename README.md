# EventStoreDB Client SDK for Java

EventStoreDB is the event-native database, where business events are immutably stored and streamed. Designed for event-sourced, event-driven, and microservices architectures.

This repository contains an [EventStoreDB][es] Client SDK written in Java for use with languages on the JVM. It is
compatible with Java 8 and above.

*Note: This client is currently under active development and further API changes are expected. Feedback is very welcome.*

## Documentation
* General documentation can be found in [Event Store GRPC Docs].
* The latest stable version Javadoc can be found here: https://eventstore.github.io/EventStoreDB-Client-Java

## Access to binaries
EventStore Ltd publishes GA (general availability) versions to [Maven Central].

### Snapshot versions

Snapshot versions are pushed on Sonatype Snapshots Repository every time a pull request is merged in the main branch `trunk`.
The snippet below shows how to use the Sonatype Snapshots Repository using the Gradle build tool.

```gradle
repositories {
    ...
    maven {
        url uri('https://oss.sonatype.org/content/repositories/snapshots')
    }
    ...
}
```

## Developing

The SDK is built using [`Gradle`][gradle]. Integration tests run against a server using Docker, with the [EventStoreDB gRPC
Client Test Container][container].

### Run tests

Tests are written using [TestContainers](https://www.testcontainers.org/) and require [Docker](https://www.docker.com/) to be installed.

Specific docker images can be specified via the enviroment variable `EVENTSTORE_IMAGE`.

## EventStoreDB Server Compatibility

This client is compatible with version `20.6.1` upwards.

Server setup instructions can be found in the [docs], follow the docker setup for the simplest configuration.

## Example

The following snippet showcases a simple example where we form a connection, then write and read events from the server.

Note: If testing locally using `--insecure` the url should be `esdb://localhost:2113?tls=false`.

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
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.WriteResult;
import com.eventstore.dbclient.ReadResult;

public class Main {
    public static void main(String args[]) {
        EventStoreDBClientSettings setts = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113");
        EventStoreDBClient client = EventStoreDBClient.create(setts);

        AccountCreated createdEvent = new AccountCreated();

        createdEvent.setId(UUID.randomUUID());
        createdEvent.setLogin("ouros");

        EventData event = EventData
                .builderAsJson("account-created", createdEvent)
                .build();

        WriteResult writeResult = client
                .appendToStream("accounts", event)
                .get();

        ReadStreamOptions readStreamOptions = ReadStreamOptions.get()
                .fromStart()
                .notResolveLinkTos();

        ReadResult readResult = client
                .readStream("accounts", 1, readStreamOptions)
                .get();

        ResolvedEvent resolvedEvent = readResult
                .getEvents()
                .get(0);

        AccountCreated writtenEvent = resolvedEvent.getOriginalEvent()
                .getEventDataAs(AccountCreated.class);

        // Doing something productive...
    }
}
```

## Projections

This client currently supports creating and getting the result of a continuous projection.

Create a projection:
```java
EventStoreDbClientSettings setts = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113");
EventStoreDBProjectionManagementClient client = EventStoreDBProjectionManagementClient.create(setts);

client
    .createContinuous(PROJECTION_NAME, PROJECTION_JS)
    .get();
```

Define a class in which to deserialize the result:
```java
public class CountResult {

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }
}
```

Get the result:
```java
CountResult result = client
    .getResult(PROJECTION_NAME, CountResult.class)
    .get();
```

For further details please see [the projection management tests](src/test/java/com/eventstore/dbclient/ProjectionManagementTests.java).

## Support

Information on support can be found on our website: [Event Store Support][support]

## Documentation

Documentation for EventStoreDB can be found in the [docs].

Bear in mind that this client is not yet properly documented. We are working hard on a new version of the documentation.

## Security

If you find a vulnerability in our software, please contact us. You can find how to reach out us and report it at https://www.eventstore.com/security#security
Thank you very much for supporting our software.

## Communities

- [Discuss](https://discuss.eventstore.com/)
- [Discord (Event Store)](https://discord.gg/Phn9pmCw3t)

## Contributing

All contributions to the SDK are made via GitHub Pull Requests, and must be licensed under the Apache 2.0 license. Please
review our [Contributing Guide][contributing] and [Code of Conduct][code-of-conduct] for more information.

[es]: https://eventstore.com
[gradle]: https://gradle.org
[container]: https://github.com/EventStore/EventStore-Client-gRPC-TestData
[contributing]: https://github.com/EventStore/EventStoreDB-Client-Java/tree/master/CONTRIBUTING.md
[code-of-conduct]: https://github.com/EventStore/EventStoreDB-Client-Java/tree/master/CODE-OF-CONDUCT.md
[support]: https://eventstore.com/support/
[docs]: https://developers.eventstore.com/server/v21.6/installation/
[discuss]: https://discuss.eventstore.com/
[Maven Central]: https://search.maven.org/artifact/com.eventstore/db-client-java
[Event Store GRPC Docs]: https://developers.eventstore.com/clients/grpc

# Event Store Client SDK for Java

This repository contains an [Event Store][es] Client SDK written in Java for use with languages on the JVM. It is
compatible with Java 8 and above.

## Developing

The SDK is built using [`Gradle`][gradle]. Integration tests run against a server using Docker, with the [Event Store gRPC
Client Test Container][container]. Packages are not currently published to Maven Central, but will be once this library
approaches release.

## Contributing

All contributions to the SDK are made via GitHub Pull Requests, and must be licensed under the Apache 2.0 license. Please
review our [Contributing Guide][contributing] and [Code of Conduct][code-of-conduct] for more information.

[es]: https://eventstore.com
[gradle]: https://gradle.org
[container]: https://github.com/EventStore/EventStore-Client-gRPC-TestData
[contributing]: https://github.com/EventStore/EventStoreDB-Client-Java/tree/master/CONTRIBUTING.md
[code-of-conduct]: https://github.com/EventStore/EventStoreDB-Client-Java/tree/master/CODE-OF-CONDUCT.md

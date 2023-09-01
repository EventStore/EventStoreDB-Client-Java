# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

### Changed
- Improve internal gRPC connection management for better error propagation. [EventStoreDB-Client-Java#226](https://github.com/EventStore/EventStoreDB-Client-Java/pull/226)

### Added
- Support authenticated gossip read request. [EventStoreDB-Client-Java#235](https://github.com/EventStore/EventStoreDB-Client-Java/pull/235)

## [4.3.0] - 2023-07-03
### Added
- Support user-provided gRPC client interceptors. [EventStoreDB-Client-Java#233](https://github.com/EventStore/EventStoreDB-Client-Java/pull/233)

## [4.2.0] - 2023-04-27

### Fixed
- Do not start discovery process on ABORT gRPC error. [EventStoreDB-Client-Java#219](https://github.com/EventStore/EventStoreDB-Client-Java/pull/219)
- Fix OptionBase authentication code. [EventStoreDB-Client-Java#221](https://github.com/EventStore/EventStoreDB-Client-Java/pull/221)

### Added
- Provide toString override for public types. [EventStoreDB-Client-Java#218](https://github.com/EventStore/EventStoreDB-Client-Java/pull/218)
- Implement `ExpectedRevision` raw long representation. [EventStoreDB-Client-Java#230](https://github.com/EventStore/EventStoreDB-Client-Java/pull/230)

### Changed
- Increase max inbound message length. [EventStoreDB-Client-Java#222](https://github.com/EventStore/EventStoreDB-Client-Java/pull/222)

## [4.1.1] - 2023-03-06

### Changed
- Stop using Jackson JsonMapper static instances. [EventStoreDB-Client-Java#217](https://github.com/EventStore/EventStoreDB-Client-Java/pull/217)

## [4.1.0] - 2023-02-24

### Added
- Add specific exceptions when delete stream operation fails. [EventStoreDB-Client-Java#208](https://github.com/EventStore/EventStoreDB-Client-Java/pull/208)
- Implement human-representation for `ExpectedVersion` types. [EventStoreDB-Client-Java#204](https://github.com/EventStore/EventStoreDB-Client-Java/pull/204)

### Fixed
- Fix server filtering sample code. [EventStoreDB-Client-Java#206](https://github.com/EventStore/EventStoreDB-Client-Java/pull/206)
- Fix `ConnectionSettingsBuilder` when dealing with keep-alive settings. [EventStoreDB-Client-Java#207](https://github.com/EventStore/EventStoreDB-Client-Java/pull/207)
- Fix `tombstoneStream` overload. [EventStoreDB-Client-Java#205](https://github.com/EventStore/EventStoreDB-Client-Java/pull/205)
- No longer store credentials unprotected in memory. [EventStoreDB-Client-Java#214](https://github.com/EventStore/EventStoreDB-Client-Java/pull/214)

### Changed
- Update gRPC and protobuf version. [EventStoreDB-Client-Java#213](https://github.com/EventStore/EventStoreDB-Client-Java/pull/213)

## [4.0.0] - 2022-11-01

### Fixed
- Fix next expected version when appending to a stream. [EventStoreDB-Client-Java#196](https://github.com/EventStore/EventStoreDB-Client-Java/pull/196)
- Fix condition causing subscribers not to be completed. [EventStoreDB-Client-Java#193](https://github.com/EventStore/EventStoreDB-Client-Java/pull/193)
- Shutdown `GossipClient` after usage. [EventStoreDB-Client-Java#186](https://github.com/EventStore/EventStoreDB-Client-Java/pull/186)
- Fix channel lifecycle behaviour. [EventStoreDB-Client-Java#184](https://github.com/EventStore/EventStoreDB-Client-Java/pull/184)
- Do not shutdown client on leader reconnect attempt. [EventStoreDB-Client-Java#182](https://github.com/EventStore/EventStoreDB-Client-Java/pull/182)
- Fix error signals to the `GrpcClient` based on a `CompletableFuture`. [EventStoreDB-Client-Java#182](https://github.com/EventStore/EventStoreDB-Client-Java/pull/182)

### Changed
- Fix next expected version when appending to a stream. [EventStoreDB-Client-Java#196](https://github.com/EventStore/EventStoreDB-Client-Java/pull/196)
- Add additional logging for connection handling. [EventStoreDB-Client-Java#181](https://github.com/EventStore/EventStoreDB-Client-Java/pull/181)

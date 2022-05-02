package com.eventstore.dbclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * Utility class to parse a connection string.
 */
public final class EventStoreDBConnectionString {
    private final Logger logger = LoggerFactory.getLogger(EventStoreDBConnectionString.class);
    private int position = 0;
    private int nextPosition = 0;
    private String connectionString;
    private final ConnectionSettingsBuilder settings = EventStoreDBClientSettings.builder();
    private final List<String> notCurrentlySupported = Arrays.asList(
            "throwOnAppendFailure"
    );
    private HashMap<String,String> LowerToKey = new HashMap<String,String>(){
        {
            put("dnsdiscover", "dnsDiscover");
            put("maxdiscoverattempts", "maxDiscoverAttempts");
            put("discoveryinterval", "discoveryInterval");
            put("gossiptimeout", "gossipTimeout");
            put("nodepreference", "nodePreference");
            put("tls", "tls");
            put("tlsverifycert", "tlsVerifyCert");
            put("throwonappendfailure", "throwOnAppendFailure");
            put("keepalivetimeout", "keepAliveTimeout");
            put("keepaliveinterval", "keepAliveInterval");
            put("defaultdeadline", "defaultDeadline");
        }
    };

    EventStoreDBConnectionString() {}

    /**
     * Parses a string representation of a client settings.
     * @return a client settings.
     * @throws ConnectionStringParsingException if the connection is malformed.
     */
    public static EventStoreDBClientSettings parse(String connectionString) throws ConnectionStringParsingException {
        return new EventStoreDBConnectionString().parseConnectionString(connectionString);
    }


    /**
     * Parses a string representation of a client settings. Throws a runtime exception if the connection string is
     * malformed.
     * @return a client settings.
     */
    public static EventStoreDBClientSettings parseOrThrow(String connectionString) {
        try {
            return EventStoreDBConnectionString.parse(connectionString);
        } catch (ConnectionStringParsingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRemaining() {
        return this.connectionString.substring(this.position);
    }

    private EventStoreDBClientSettings parseConnectionString(String connectionString) throws ConnectionStringParsingException {
        this.connectionString = connectionString.trim().replaceAll("/+$", "");
        return this.parseProtocol();
    }

    private EventStoreDBClientSettings parseProtocol() throws ConnectionStringParsingException {
        this.position = nextPosition;
        String expected = "esdb:// or esdb+discover://";
        String pattern = "^(?<protocol>[^:]+)://";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.getRemaining());
        boolean found = m.find();

        if (found && !m.group("protocol").isEmpty()) {
            this.nextPosition += m.end();

            switch (m.group("protocol")) {
                case "esdb": {
                    this.settings.dnsDiscover(false);
                    return this.parseCredentials();
                }
                case "esdb+discover": {
                    this.settings.dnsDiscover(true);
                    return this.parseCredentials();
                }
            }
        }

        throw new ConnectionStringParsingException(this.connectionString, this.position, this.nextPosition, expected);
    }

    private EventStoreDBClientSettings parseCredentials() throws ConnectionStringParsingException {
        this.position = nextPosition;
        String expected = "<URL encoded username>:<Url encoded password>";
        String pattern = "^(?:(?<credentials>[^:]+:[^@]+)@)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.getRemaining());
        boolean found = m.find();

        // This is optional
        if (!found) {
            return this.parseHosts(true);
        }

        if (!m.group("credentials").isEmpty()) {
            try {
                this.nextPosition += m.end();
                String[] credentials = m.group("credentials").split(":");
                String username = URLDecoder.decode(credentials[0], "utf-8");
                String password = URLDecoder.decode(credentials[1], "utf-8");
                this.settings.defaultCredentials(username, password);
            } catch (UnsupportedEncodingException e) {
                throw new ConnectionStringParsingException(this.connectionString, this.position, this.nextPosition, expected);
            }

            return this.parseHosts(true);
        }

        throw new ConnectionStringParsingException(this.connectionString, this.position, this.nextPosition, expected);
    }

    private EventStoreDBClientSettings parseHosts(boolean mustMatch) throws ConnectionStringParsingException {
        this.position = nextPosition;
        String expected = "<URL encoded username>:<Url encoded password>";
        String pattern = "^(?:(?<host>[^$+!?*'(),;\\[\\]{}|\"%~#<>=&/]+)[,/]?)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.getRemaining());
        boolean found = m.find();

        if (!found && mustMatch) {
            throw new ConnectionStringParsingException(this.connectionString, this.position, this.nextPosition, expected);
        }

        if (found && !m.group("host").isEmpty()) {
            this.nextPosition += m.end();

            String[] hostParts = m.group("host").split(":");
            String address = hostParts[0];
            String rawPort = hostParts.length > 1 ? hostParts[1] : "2113";

            if (hostParts.length > 2) {
                throw new ConnectionStringParsingException(
                        this.connectionString,
                        this.position + (address + ":" + rawPort).length(),
                        this.nextPosition,
                        ", or ?key=value");
            }

            try {
                int port = parseInt(rawPort);
                Endpoint host = new Endpoint(address, port);
                this.settings.addHost(host);
            } catch (NumberFormatException e) {
                throw new ConnectionStringParsingException(
                        this.connectionString,
                        this.position + address.length(),
                        this.nextPosition,
                        "port number"
                );
            }

            return parseHosts(false);
        }

        return this.parseSearchParams(true);
    }

    static Optional<NodePreference> parseNodePreference(String value) {
        switch (value.toLowerCase()) {
            case "leader":
                return Optional.of(NodePreference.LEADER);
            case "follower":
                return Optional.of(NodePreference.FOLLOWER);
            case "readonlyreplica":
                return Optional.of(NodePreference.READ_ONLY_REPLICA);
            case "random":
                return Optional.of(NodePreference.RANDOM);
            default:
                return Optional.empty();
        }
    }

    private static final String[] NODE_PREFERENCE_VALUES = new String[] { "leader", "follower", "readonlyreplica","random" };

    private EventStoreDBClientSettings parseSearchParams(boolean first) throws ConnectionStringParsingException {
        this.position = nextPosition;
        if (this.position == this.connectionString.length()) {
            return this.settings.buildConnectionSettings();
        }
        String expected = first ? "?key=value" : "&key=value";
        String pattern = "^(?:" + (first ?  "\\?" : "&") + "(?<key>[^=]+)=(?<value>[^&?]+))";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.getRemaining());
        boolean found = m.find();

        if (!found || m.group("key").isEmpty() || m.group("value").isEmpty()) {
            throw new ConnectionStringParsingException(this.connectionString, this.position, this.nextPosition, expected);
        }

        this.nextPosition += m.end();

        String rawKey = m.group("key");
        String key = this.LowerToKey.getOrDefault(rawKey.toLowerCase(Locale.ROOT), rawKey);
        String value = m.group("value");
        int keyPosition = this.position + ("&" + key + "=").length();

        if (notCurrentlySupported.contains(key)) {
            logger.warn("{} is not currently supported by this client, and will have no effect.", key);
        }

        switch (key.toLowerCase()) {
            case "nodepreference": {
                Optional<NodePreference> preference = parseNodePreference(value);
                if (preference.isPresent()) {
                    this.settings.nodePreference(preference.get());
                } else {
                    throw new ConnectionStringParsingException(this.connectionString, keyPosition, this.nextPosition,
                            Arrays.toString(NODE_PREFERENCE_VALUES));
                }
                break;
            }
            case "maxdiscoverattempts": {
                try {
                    int parsedValue = parseInt(value);
                    this.settings.maxDiscoverAttempts(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ConnectionStringParsingException(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
                break;
            }
            case "discoveryinterval": {
                try {
                    int parsedValue = parseInt(value);
                    this.settings.discoveryInterval(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ConnectionStringParsingException(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
                break;
            }
            case "gossiptimeout": {
                try {
                    int parsedValue = parseInt(value);
                    this.settings.gossipTimeout(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ConnectionStringParsingException(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
                break;
            }
            case "dnsdiscover": {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ConnectionStringParsingException(this.connectionString, keyPosition, this.nextPosition, "true or false");
                }
                this.settings.dnsDiscover(value.equals("true"));
                break;
            }
            case "tls": {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ConnectionStringParsingException(this.connectionString, keyPosition, this.nextPosition, "true or false");
                }
                this.settings.tls(value.equals("true"));
                break;
            }
            case "tlsverifycert": {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ConnectionStringParsingException(this.connectionString, keyPosition, this.nextPosition, "true or false");
                }
                this.settings.tlsVerifyCert(value.equals("true"));
                break;
            }
            case "throwonappendfailure": {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ConnectionStringParsingException(this.connectionString, keyPosition, this.nextPosition, "true or false");
                }
                this.settings.throwOnAppendFailure(value.equals("true"));
                break;
            }
            case "keepalivetimeout": {
                try {
                    long parsedValue = parseLong(value);
                    if (parsedValue >= 0 && parsedValue < Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS) {
                        logger.warn("Specified keepAliveTimeout of {} is less than recommended {}", parsedValue, Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS);
                    }

                    if (parsedValue < -1) {
                        logger.error("Invalid keepAliveTimeout of {}. Please provide a positive integer, or -1 to disable.", parsedValue);

                        throw new ConnectionStringParsingException(
                                this.connectionString,
                                keyPosition,
                                this.nextPosition,
                                "positive integer"
                        );
                    }

                    if (parsedValue == -1)
                        parsedValue = Long.MAX_VALUE;

                    this.settings.keepAliveTimeout(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ConnectionStringParsingException(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
                break;
            }
            case "keepaliveinterval": {
                try {
                    long parsedValue = parseLong(value);
                    if (parsedValue >= 0 && parsedValue < Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS) {
                        logger.warn("Specified keepAliveInterval of {} is less than recommended {}", parsedValue, Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS);
                    }

                    if (parsedValue < -1) {
                        logger.error("Invalid keepAliveInterval of {}. Please provide a positive integer, or -1 to disable.", parsedValue);

                        throw new ConnectionStringParsingException(
                                this.connectionString,
                                keyPosition,
                                this.nextPosition,
                                "positive integer"
                        );
                    }

                    if (parsedValue == -1)
                        parsedValue = Long.MAX_VALUE;

                    this.settings.keepAliveInterval(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ConnectionStringParsingException(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
                break;
            }
            case "defaultdeadline":
                try {
                    long parsedValue = parseLong(value);

                    if (parsedValue <= 0) {
                        logger.error("Invalid defaultDeadline of {}. Please provide a strictly positive integer", parsedValue);

                        throw new ConnectionStringParsingException(
                                this.connectionString,
                                keyPosition,
                                this.nextPosition,
                                "positive integer"
                        );
                    }

                    this.settings.defaultDeadline(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ConnectionStringParsingException(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
            default: {
                logger.warn("Unknown option {}, setting will be ignored.", key);
            }
        }


        return this.parseSearchParams(false);
    }


}

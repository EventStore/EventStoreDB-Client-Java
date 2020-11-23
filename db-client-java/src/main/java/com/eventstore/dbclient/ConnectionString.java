package com.eventstore.dbclient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class ConnectionString {
    private int position = 0;
    private int nextPosition = 0;
    private String connectionString;
    private final ConnectionSettingsBuilder settings = ConnectionSettings.builder();
    private final List<String> notCurrentlySupported = Arrays.asList(
            "maxDiscoverAttempts",
            "discoveryInterval",
            "gossipTimeout",
            "throwOnAppendFailure",
            "defaultCredentials"
    );

    public static ConnectionSettings parse(String connectionString) throws ParseError {
        return new ConnectionString().parseConnectionString(connectionString);
    }

    private String getRemaining() {
        return this.connectionString.substring(this.position);
    }

    private ConnectionSettings parseConnectionString(String connectionString) throws ParseError {
        this.connectionString = connectionString.trim().replaceAll("/+$", "");
        return this.parseProtocol();
    }

    private ConnectionSettings parseProtocol() throws ParseError {
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

        throw new ParseError(this.connectionString, this.position, this.nextPosition, expected);
    }

    private ConnectionSettings parseCredentials() throws ParseError {
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
                String password = URLDecoder.decode(credentials[0], "utf-8");
                this.settings.defaultCredentials(username, password);
            } catch (UnsupportedEncodingException e) {
                throw new ParseError(this.connectionString, this.position, this.nextPosition, expected);
            }

            return this.parseHosts(true);
        }

        throw new ParseError(this.connectionString, this.position, this.nextPosition, expected);
    }

    private ConnectionSettings parseHosts(boolean mustMatch) throws ParseError {
        this.position = nextPosition;
        String expected = "<URL encoded username>:<Url encoded password>";
        String pattern = "^(?:(?<host>[^$+!?*'(),;\\[\\]{}|\"%~#<>=&/]+)[,/]?)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.getRemaining());
        boolean found = m.find();

        if (!found && mustMatch) {
            throw new ParseError(this.connectionString, this.position, this.nextPosition, expected);
        }

        if (found && !m.group("host").isEmpty()) {
            this.nextPosition += m.end();

            String[] hostParts = m.group("host").split(":");
            String address = hostParts[0];
            String rawPort = hostParts.length > 1 ? hostParts[1] : "2113";

            if (hostParts.length > 2) {
                throw new ParseError(
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
                throw new ParseError(
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

    private ConnectionSettings parseSearchParams(boolean first) throws ParseError {
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
            throw new ParseError(this.connectionString, this.position, this.nextPosition, expected);
        }

        this.nextPosition += m.end();

        String key = m.group("key");
        String value = m.group("value");
        int keyPosition = this.position + ("&" + key + "=").length();

        if (notCurrentlySupported.contains(key)) {
            String warning = key + " is not currently supported by this client, and will have no effect.";
            System.out.println(warning);
        }

        switch (key) {
            case "nodePreference": {
                try {
                    NodePreference preference = NodePreference.valueOf(value.toUpperCase());
                    this.settings.nodePreference(preference);
                } catch (IllegalArgumentException e) {
                    throw new ParseError(this.connectionString, keyPosition, this.nextPosition, Arrays.toString(NodePreference.values()));
                }
                break;
            }
            case "maxDiscoverAttempts": {
                try {
                    int parsedValue = parseInt(value);
                    this.settings.maxDiscoverAttempts(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ParseError(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
                break;
            }
            case "discoveryInterval": {
                try {
                    int parsedValue = parseInt(value);
                    this.settings.discoveryInterval(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ParseError(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
                break;
            }
            case "gossipTimeout": {
                try {
                    int parsedValue = parseInt(value);
                    this.settings.gossipTimeout(parsedValue);
                } catch (NumberFormatException e) {
                    throw new ParseError(
                            this.connectionString,
                            keyPosition,
                            this.nextPosition,
                            "integer"
                    );
                }
                break;
            }
            case "dnsDiscover": {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ParseError(this.connectionString, keyPosition, this.nextPosition, "true or false");
                }
                this.settings.dnsDiscover(value.equals("true"));
                break;
            }
            case "tls": {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ParseError(this.connectionString, keyPosition, this.nextPosition, "true or false");
                }
                this.settings.tls(value.equals("true"));
                break;
            }
            case "tlsVerifyCert": {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ParseError(this.connectionString, keyPosition, this.nextPosition, "true or false");
                }
                this.settings.tlsVerifyCert(value.equals("true"));
                break;
            }
            case "throwOnAppendFailure": {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ParseError(this.connectionString, keyPosition, this.nextPosition, "true or false");
                }
                this.settings.throwOnAppendFailure(value.equals("true"));
                break;
            }
            default: {
                String warning = "unknown option " + key + ", setting will be ignored.";
                System.out.println(warning);
            }
        }


        return this.parseSearchParams(false);
    }


}

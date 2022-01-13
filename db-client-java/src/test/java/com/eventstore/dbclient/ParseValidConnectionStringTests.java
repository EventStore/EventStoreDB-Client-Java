package com.eventstore.dbclient;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ParseValidConnectionStringTests {
    private final JsonMapper mapper = new JsonMapper();


    @Parameterized.Parameters
    public static Collection validConnectionStrings() {
        return Arrays.asList(new Object[][]{
                {
                        "esdb://localhost",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2113}]}"
                },
                {
                        "esdb://localhost:2114",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                },
                {
                        "esdb://user:pass@localhost:2114",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                },
                {
                        "esdb://user:pass@localhost:2114/",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                },
                {
                        "esdb://user:pass@localhost:2114/?tlsVerifyCert=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                },
                {
                        "esdb://user:pass@localhost:2114?tlsVerifyCert=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                },
                {
                        "esdb://user:pass@localhost:2114?tls=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                },
                {
                        "esdb://host1,host2,host3",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"host1\",\"port\":2113},{\"address\":\"host2\",\"port\":2113},{\"address\":\"host3\",\"port\":2113}]}"
                },
                {
                        "esdb://host1:1234,host2:4321,host3:3231",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"host1\",\"port\":1234},{\"address\":\"host2\",\"port\":4321},{\"address\":\"host3\",\"port\":3231}]}"
                },
                {
                        "esdb://bubaqp2rh41uf5akmj0g-0.mesdb.eventstore.cloud:2113,bubaqp2rh41uf5akmj0g-1.mesdb.eventstore.cloud:2113,bubaqp2rh41uf5akmj0g-2.mesdb.eventstore.cloud:2113",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"bubaqp2rh41uf5akmj0g-0.mesdb.eventstore.cloud\",\"port\":2113},{\"address\":\"bubaqp2rh41uf5akmj0g-1.mesdb.eventstore.cloud\",\"port\":2113},{\"address\":\"bubaqp2rh41uf5akmj0g-2.mesdb.eventstore.cloud\",\"port\":2113}]}"
                },
                {
                        "esdb://user:pass@host1:1234,host2:4321,host3:3231?nodePreference=follower",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"follower\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"host1\",\"port\":1234},{\"address\":\"host2\",\"port\":4321},{\"address\":\"host3\",\"port\":3231}]}"
                },
                {
                        "esdb://host1,host2,host3?tls=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"host1\",\"port\":2113},{\"address\":\"host2\",\"port\":2113},{\"address\":\"host3\",\"port\":2113}]}"
                },
                {
                        "esdb://127.0.0.1:21573?tls=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"127.0.0.1\",\"port\":21573}]}"
                },
                {
                        "esdb://host1,host2,host3?tlsVerifyCert=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"host1\",\"port\":2113},{\"address\":\"host2\",\"port\":2113},{\"address\":\"host3\",\"port\":2113}]}"
                },
                {
                        "esdb+discover://user:pass@host?nodePreference=follower&tlsVerifyCert=false",
                        "{\"dnsDiscover\":true,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"follower\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"host\",\"port\":2113}]}"
                },
                {
                        "esdb://my%3Agreat%40username:UyeXx8%24%5EPsOo4jG88FlCauR1Coz25q@host?nodePreference=follower&tlsVerifyCert=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"follower\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"my:great@username\",\"password\":\"UyeXx8$^PsOo4jG88FlCauR1Coz25q\"},\"hosts\":[{\"address\":\"host\",\"port\":2113}]}"
                },
                {
                        "esdb://host?maxDiscoverAttempts=200&discoveryInterval=1000&gossipTimeout=1&nodePreference=leader&tls=false&tlsVerifyCert=false&throwOnAppendFailure=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":200,\"discoveryInterval\":1000,\"gossipTimeout\":1,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":false,\"hosts\":[{\"address\":\"host\",\"port\":2113}]}"
                },
                {
                        "esdb://host?MaxDiscoverAttempts=200&discoveryinterval=1000&GOSSIPTIMEOUT=1&nOdEpReFeReNcE=leader&TLS=false&TlsVerifyCert=false&THROWOnAppendFailure=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":200,\"discoveryInterval\":1000,\"gossipTimeout\":1,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":false,\"hosts\":[{\"address\":\"host\",\"port\":2113}]}"
                },
                {
                        "esdb://localhost?keepAliveTimeout=20&keepAliveInterval=10",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2113}], \"keepAliveTimeout\": \"20\", \"keepAliveInterval\": \"10\"}"
                },
                {
                        "esdb://localhost?keepAliveTimeout=20&keepAliveInterval=10&nodePreference=readOnlyReplica",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"readOnlyReplica\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2113}], \"keepAliveTimeout\": \"20\", \"keepAliveInterval\": \"10\"}"
                },
        });
    }

    public void assertEquals(EventStoreDBClientSettings settings, EventStoreDBClientSettings other) {
        Assert.assertEquals(settings.isDnsDiscover(), other.isDnsDiscover());
        Assert.assertEquals(settings.getMaxDiscoverAttempts(), other.getMaxDiscoverAttempts());
        Assert.assertEquals(settings.getDiscoveryInterval(), other.getDiscoveryInterval());
        Assert.assertEquals(settings.getGossipTimeout(), other.getGossipTimeout());
        Assert.assertEquals(settings.getNodePreference(), other.getNodePreference());
        Assert.assertEquals(settings.isTls(), other.isTls());
        Assert.assertEquals(settings.isTlsVerifyCert(), other.isTlsVerifyCert());
        Assert.assertEquals(settings.isThrowOnAppendFailure(), other.isThrowOnAppendFailure());
        Assert.assertEquals(settings.getKeepAliveTimeout(), other.getKeepAliveTimeout());
        Assert.assertEquals(settings.getKeepAliveInterval(), other.getKeepAliveInterval());

        Assert.assertEquals(settings.getHosts().length, other.getHosts().length);
        IntStream.range(0, settings.getHosts().length).forEach((i) -> {
            Assert.assertEquals(settings.getHosts()[i].getHostname(), other.getHosts()[i].getHostname());
            Assert.assertEquals(settings.getHosts()[i].getPort(), other.getHosts()[i].getPort());
        });
    }

    @Parameterized.Parameter
    public String connectionString;

    @Parameterized.Parameter(1)
    public String jsonSettings;

    @Test
    public void test() throws ParseError, JsonProcessingException {

        EventStoreDBClientSettings expectedSettings = this.parseJson(jsonSettings);
        EventStoreDBClientSettings parsedSettings = EventStoreDBConnectionString.parse(connectionString);

        this.assertEquals(expectedSettings, parsedSettings);
    }

    private EventStoreDBClientSettings parseJson(String input) throws JsonProcessingException {
        ConnectionSettingsBuilder builder = EventStoreDBClientSettings.builder();
        JsonNode tree = mapper.readTree(input);

        if (tree.get("dnsDiscover") != null)
            builder.dnsDiscover(tree.get("dnsDiscover").asBoolean());

        if (tree.get("maxDiscoverAttempts") != null)
            builder.maxDiscoverAttempts(tree.get("maxDiscoverAttempts").asInt());

        if (tree.get("discoveryInterval") != null)
            builder.discoveryInterval(tree.get("discoveryInterval").asInt());

        if (tree.get("gossipTimeout") != null)
            builder.gossipTimeout(tree.get("gossipTimeout").asInt());

        if (tree.get("tls") != null)
            builder.tls(tree.get("tls").asBoolean());

        if (tree.get("tlsVerifyCert") != null)
            builder.tlsVerifyCert(tree.get("tlsVerifyCert").asBoolean());

        if (tree.get("throwOnAppendFailure") != null)
            builder.throwOnAppendFailure(tree.get("throwOnAppendFailure").asBoolean());

        if (tree.get("keepAliveTimeout") != null)
            builder.keepAliveTimeout(Long.parseLong(tree.get("keepAliveTimeout").asText()));

        if (tree.get("keepAliveInterval") != null)
            builder.keepAliveInterval(Long.parseLong(tree.get("keepAliveInterval").asText()));

        if (tree.get("nodePreference") != null)
            builder.nodePreference(EventStoreDBConnectionString.parseNodePreference(tree.get("nodePreference").asText()).get());

        tree.get("hosts").elements().forEachRemaining((host) -> {
            builder.addHost(new Endpoint(host.get("address").asText(), host.get("port").asInt()));
        });


        return builder.buildConnectionSettings();
    }
}

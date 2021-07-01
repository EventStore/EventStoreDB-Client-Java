package com.eventstore.dbclient;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ParseInvalidConnectionStringTests {
    private final JsonMapper mapper = new JsonMapper();


    @Parameterized.Parameters
    public static Collection validConnectionStrings() {
        return Arrays.asList(new Object[][]{
                {"localhost"},
                {"https://console.eventstore.cloud/"},
                {"esbd+discovery://localhost"},
                {"esdb://my:great@username:UyeXx8$^PsOo4jG88FlCauR1Coz25q@host?nodePreference=follower&tlsVerifyCert=false"},
                {"esdb://host1;host2;host3?tlsVerifyCert=false"},
                {"esdb://host1,host2:200:300?tlsVerifyCert=false"},
                {"esdb://tlsVerifyCert=false"},
                {"esdb://localhost/&tlsVerifyCert=false"},
                {"esdb://localhost?tlsVerifyCert=false?nodePreference=follower"},
                {"esdb://localhost?tlsVerifyCert=false&nodePreference=any"},
                {"esdb://localhost?tlsVerifyCert=if you feel like it"},
                {"esdb://localhost?throwOnAppendFailure=sometimes"},
                {"esdb://localhost?keepAliveInterval=-3"},
                {"esdb://localhost?keepAliveInterval=sdfksjsfl"},
                {"esdb://localhost?keepAliveTimeout=sdfksjsfl"},
                {"esdb://localhost?keepAliveTimeout=-3"},
                {"esdb://localhost?nodePreference=read_only_replica"},
        });
    }

    @Parameterized.Parameter
    public String connectionString;


    @Test(expected = ParseError.class)
    public void test() throws ParseError {
        EventStoreDBClientSettings parsedSettings = EventStoreDBConnectionString.parse(connectionString);
    }

}

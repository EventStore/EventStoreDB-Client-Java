package com.eventstore.dbclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

public class ParseInvalidConnectionStringTests {
    public static Stream<Arguments> invalidConnectionStrings() {
        return Stream.of(
                Arguments.of("localhost"),
                Arguments.of("https://console.eventstore.cloud/"),
                Arguments.of("esbd+discovery://localhost"),
                Arguments.of("esdb://my:great@username:UyeXx8$^PsOo4jG88FlCauR1Coz25q@host?nodePreference=follower&tlsVerifyCert=false"),
                Arguments.of("esdb://host1;host2;host3?tlsVerifyCert=false"),
                Arguments.of("esdb://host1,host2:200:300?tlsVerifyCert=false"),
                Arguments.of("esdb://tlsVerifyCert=false"),
                Arguments.of("esdb://localhost/&tlsVerifyCert=false"),
                Arguments.of("esdb://localhost?tlsVerifyCert=false?nodePreference=follower"),
                Arguments.of("esdb://localhost?tlsVerifyCert=false&nodePreference=any"),
                Arguments.of("esdb://localhost?tlsVerifyCert=if you feel like it"),
                Arguments.of("esdb://localhost?throwOnAppendFailure=sometimes"),
                Arguments.of("esdb://localhost?keepAliveInterval=-3"),
                Arguments.of("esdb://localhost?keepAliveInterval=sdfksjsfl"),
                Arguments.of("esdb://localhost?keepAliveTimeout=sdfksjsfl"),
                Arguments.of("esdb://localhost?keepAliveTimeout=-3"),
                Arguments.of("esdb://localhost?nodePreference=read_only_replica")
       );
    }

    @ParameterizedTest
    @MethodSource("invalidConnectionStrings")
    public void test(String input) throws ConnectionStringParsingException {
        Assertions.assertThrows(ConnectionStringParsingException.class, () -> {
            EventStoreDBClientSettings parsedSettings = EventStoreDBConnectionString.parse(input);
        });
    }
}

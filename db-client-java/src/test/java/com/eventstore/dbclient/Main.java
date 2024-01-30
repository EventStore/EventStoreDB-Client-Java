package com.eventstore.dbclient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        EventStoreDBClientSettings settings = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
        EventStoreDBProjectionManagementClient client = EventStoreDBProjectionManagementClient.create(settings);

        String js =
                "fromAll()" +
                        ".when({" +
                        "    $init: function() {" +
                        "        return {" +
                        "            count: 0" +
                        "        };" +
                        "    }," +
                        "    $any: function(s, e) {" +
                        "        s.count += 1;" +
                        "    }" +
                        "})" +
                        ".outputState();";

        String name = "countEvents_Create_" + java.util.UUID.randomUUID();

        client.create(name, js, CreateProjectionOptions.get().enabled(true)).get();
    }
}

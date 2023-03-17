package com.eventstore.dbclient;

import java.net.InetSocketAddress;
import java.util.UUID;

interface MsgHandler {
    void createChannel(UUID previousId, InetSocketAddress endpoint);
    void process(RunWorkItem args);
    void shutdown(Shutdown args);
}

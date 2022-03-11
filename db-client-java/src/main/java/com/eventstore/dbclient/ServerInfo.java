package com.eventstore.dbclient;

public class ServerInfo {
    private final ServerVersion version;
    private final int features;

    public ServerInfo(ServerVersion version, int features) {
        this.version = version;
        this.features = features;
    }

    public boolean supportFeature(int feature) {
        return (features & feature) != 0;
    }
}

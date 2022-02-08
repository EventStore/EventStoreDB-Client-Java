package com.eventstore.dbclient;

import java.net.MalformedURLException;
import java.net.URL;

public class Endpoint {
    final private String hostname;
    final private int port;


    public Endpoint(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public URL getURL(boolean secure, String path) {
        String protocol = secure ? "https" : "http";
        try {
            return new URL(protocol + "://" + hostname + ":" + port + path);
        } catch (MalformedURLException e) {
           throw new RuntimeException(e);
        }
    }
}

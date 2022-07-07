package com.eventstore.dbclient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A database node endpoint.
 */
public class Endpoint {
    final private String hostname;
    final private int port;


    public Endpoint(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Returns the endpoint's hostname.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the endpoint's port.
     */
    public int getPort() {
        return port;
    }

    URL getURL(boolean secure, String path) {
        String protocol = secure ? "https" : "http";
        try {
            return new URL(protocol + "://" + hostname + ":" + port + path);
        } catch (MalformedURLException e) {
           throw new RuntimeException(e);
        }
    }
}

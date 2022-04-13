package com.eventstore.dbclient;

import java.util.List;

public abstract class PersistentSubscriptionInfo {
    private String eventSource;
    private String groupName;
    private String status;
    private List<PersistentSubscriptionConnectionInfo> connections;

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PersistentSubscriptionConnectionInfo> getConnections() {
        return connections;
    }

    public void setConnections(List<PersistentSubscriptionConnectionInfo> connections) {
        this.connections = connections;
    }
}

package com.eventstore.dbclient;

interface WorkItem {
    void accept(WorkItemArgs args, Exception error);
}
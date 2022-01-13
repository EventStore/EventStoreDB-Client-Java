package com.eventstore.dbclient;

public enum PersistentSubscriptionStatus {
    NotReady,
    Behind,
    OutstandingPageRequest,
    ReplayingParkedMessages,
    Live,
}

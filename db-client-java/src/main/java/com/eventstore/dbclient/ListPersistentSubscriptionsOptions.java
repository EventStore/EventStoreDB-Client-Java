package com.eventstore.dbclient;

/**
 * Options of the list persistent subscriptions request.
 */
public class ListPersistentSubscriptionsOptions extends CallOptionsBase<ListPersistentSubscriptionsOptions> {
    ListPersistentSubscriptionsOptions(){}

    /**
     * Returns options with default values.
     */
    public static ListPersistentSubscriptionsOptions get() {
        return new ListPersistentSubscriptionsOptions();
    }
}

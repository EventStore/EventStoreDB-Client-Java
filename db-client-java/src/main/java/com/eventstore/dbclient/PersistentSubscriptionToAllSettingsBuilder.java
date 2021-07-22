package com.eventstore.dbclient;

public class PersistentSubscriptionToAllSettingsBuilder
        extends AbstractPersistentSubscriptionSettingsBuilder<PersistentSubscriptionToAllSettingsBuilder> {
    private Position position;

    public PersistentSubscriptionToAllSettingsBuilder() {
        position = Position.START;
    }

    public PersistentSubscriptionToAllSettingsBuilder(PersistentSubscriptionToAllSettings settings) {
        super(settings);

        position = settings.getPosition();
    }

    public PersistentSubscriptionToAllSettings build() {
        return new PersistentSubscriptionToAllSettings(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize,
                liveBufferSize, maxCheckpointCount, maxRetryCount, maxSubscriberCount, messageTimeoutMs,
                minCheckpointCount, readBatchSize, strategy, fromStart, fromEnd, position);
    }

    public PersistentSubscriptionToAllSettingsBuilder position(long commitUnsigned, long prepareUnsigned) {
        return position(new Position(commitUnsigned, prepareUnsigned));
    }

    public PersistentSubscriptionToAllSettingsBuilder position(Position value) {
        this.position = value;
        return this;
    }
}

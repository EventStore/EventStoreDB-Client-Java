package com.eventstore.dbclient;

public class PersistentSubscriptionToAllSettingsBuilder
        extends AbstractPersistentSubscriptionSettingsBuilder<PersistentSubscriptionToAllSettingsBuilder> {
    private Position position;

    public PersistentSubscriptionToAllSettingsBuilder() {
        position = Position.END;
    }

    public PersistentSubscriptionToAllSettingsBuilder(PersistentSubscriptionToAllSettings settings) {
        super(settings);

        position = settings.getPosition();
    }

    public PersistentSubscriptionToAllSettings build() {
        return new PersistentSubscriptionToAllSettings(checkpointAfterMs, extraStatistics, resolveLinkTos, historyBufferSize,
                liveBufferSize, checkPointUpperBound, maxRetryCount, maxSubscriberCount, messageTimeoutMs,
                checkPointLowerBound, readBatchSize, consumerStrategyName, position);
    }

    public PersistentSubscriptionToAllSettingsBuilder fromStart() {
        return this.startFrom(Position.START);
    }

    public PersistentSubscriptionToAllSettingsBuilder fromEnd() {
        return this.startFrom(Position.END);
    }

    /**
     * The exclusive position in the stream or transaction file the subscription should start from. Default: End of stream.
     */
    public PersistentSubscriptionToAllSettingsBuilder startFrom(long commitUnsigned, long prepareUnsigned) {
        return startFrom(new Position(commitUnsigned, prepareUnsigned));
    }

    /**
     * The exclusive position in the stream or transaction file the subscription should start from. Default: End of stream.
     */
    public PersistentSubscriptionToAllSettingsBuilder startFrom(Position value) {
        this.position = value;
        return this;
    }
}

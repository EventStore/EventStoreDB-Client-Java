package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;

public class PersistentSubscriptionSettingsBuilder {
    private int checkpointAfterMs;
    private boolean extraStatistics;
    private boolean resolveLinks;
    private int historyBufferSize;
    private int liveBufferSize;
    private int maxCheckpointCount;
    private int maxRetryCount;
    private int maxSubscriberCount;
    private int messageTimeoutMs;
    private int minCheckpointCount;
    private int readBatchSize;
    private long revision;
    private ConsumerStrategy strategy;

    public PersistentSubscriptionSettingsBuilder() {
        checkpointAfterMs = 2_000;
        resolveLinks = false;
        extraStatistics = false;
        revision = 0;
        messageTimeoutMs = 30_000;
        maxRetryCount = 10;
        minCheckpointCount = 10;
        maxCheckpointCount = 1_000;
        maxSubscriberCount = 0;
        liveBufferSize = 500;
        readBatchSize = 20;
        historyBufferSize = 500;
        strategy = ConsumerStrategy.RoundRobin;
    }

    public PersistentSubscriptionSettingsBuilder(PersistentSubscriptionSettings settings) {
        checkpointAfterMs = settings.getCheckpointAfterMs();
        resolveLinks = settings.isResolveLinks();
        extraStatistics = settings.isExtraStatistics();
        revision = settings.getRevision();
        messageTimeoutMs = settings.getMessageTimeoutMs();
        maxRetryCount = settings.getMaxRetryCount();
        minCheckpointCount = settings.getMinCheckpointCount();
        maxCheckpointCount = settings.getMaxCheckpointCount();
        maxSubscriberCount = settings.getMaxSubscriberCount();
        liveBufferSize = settings.getLiveBufferSize();
        readBatchSize = settings.getReadBatchSize();
        historyBufferSize = settings.getHistoryBufferSize();
        strategy = settings.getStrategy();
    }

    public PersistentSubscriptionSettings build() {
        return new PersistentSubscriptionSettings(checkpointAfterMs, extraStatistics, resolveLinks, historyBufferSize, liveBufferSize, maxCheckpointCount, maxRetryCount, maxSubscriberCount, messageTimeoutMs, minCheckpointCount, readBatchSize, revision, strategy);
    }

    public PersistentSubscriptionSettingsBuilder enableLinkResolution() {
        return resolveLinks(true);
    }

    public PersistentSubscriptionSettingsBuilder disableLinkResolution() {
        return resolveLinks(false);
    }

    public PersistentSubscriptionSettingsBuilder resolveLinks(boolean value) {
        this.resolveLinks = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder enableExtraStatistics() {
        return extraStatistics(true);
    }

    public PersistentSubscriptionSettingsBuilder disableExtraStatistics() {
        return extraStatistics(false);
    }

    public PersistentSubscriptionSettingsBuilder extraStatistics(boolean value) {
        this.extraStatistics = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder checkpointAfterInMs(int value) {
        this.checkpointAfterMs = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder revision(long value) {
        this.revision = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder fromStreamStart() {
        return revision(0);
    }

    public PersistentSubscriptionSettingsBuilder historyBufferSize(int value) {
        this.historyBufferSize = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder liveBufferSize(int value) {
        this.liveBufferSize = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder maxCheckpointCount(int value) {
        this.maxCheckpointCount = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder minCheckpointCount(int value) {
        this.minCheckpointCount = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder maxSubscriberCount(int value) {
        this.maxSubscriberCount = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder maxRetryCount(int value) {
        this.maxRetryCount = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder messageTimeoutInMs(int value) {
        this.messageTimeoutMs = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder readBatchSize(int value) {
        this.readBatchSize = value;
        return this;
    }

    public PersistentSubscriptionSettingsBuilder consumerStrategy(ConsumerStrategy strategy) {
        this.strategy = strategy;
        return this;
    }
}

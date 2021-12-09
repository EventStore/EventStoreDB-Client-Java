package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;

public class ProjectionDetails {
    private final long coreProcessingTime;
    private final long version;
    private final long epoch;
    private final String effectiveName;
    private final int writesInProgress;
    private final int readsInProgress;
    private final int partitionsCached;
    private final String status;
    private final String stateReason;
    private final String name;
    private final String mode;
    private final String position;
    private final float progress;
    private final String lastCheckpoint;
    private final long eventsProcessedAfterRestart;
    private final String checkpointStatus;
    private final long bufferedEvents;
    private final int writePendingEventsBeforeCheckpoint;
    private final int writePendingEventsAfterCheckpoint;

    public ProjectionDetails(long coreProcessingTime, long version, long epoch, String effectiveName,
                             int writesInProgress, int readsInProgress, int partitionsCached, String status,
                             String stateReason, String name, String mode, String position, float progress,
                             String lastCheckpoint, long eventsProcessedAfterRestart, String checkpointStatus,
                             long bufferedEvents, int writePendingEventsBeforeCheckpoint,
                             int writePendingEventsAfterCheckpoint) {

        this.coreProcessingTime = coreProcessingTime;
        this.version = version;
        this.epoch = epoch;
        this.effectiveName = effectiveName;
        this.writesInProgress = writesInProgress;
        this.readsInProgress = readsInProgress;
        this.partitionsCached = partitionsCached;
        this.status = status;
        this.stateReason = stateReason;
        this.name = name;
        this.mode = mode;
        this.position = position;
        this.progress = progress;
        this.lastCheckpoint = lastCheckpoint;
        this.eventsProcessedAfterRestart = eventsProcessedAfterRestart;
        this.checkpointStatus = checkpointStatus;
        this.bufferedEvents = bufferedEvents;
        this.writePendingEventsBeforeCheckpoint = writePendingEventsBeforeCheckpoint;
        this.writePendingEventsAfterCheckpoint = writePendingEventsAfterCheckpoint;
    }

    public static ProjectionDetails fromWire(Projectionmanagement.StatisticsResp.Details details) {
        return new ProjectionDetails(
            details.getCoreProcessingTime(),
            details.getVersion(),
            details.getEpoch(),
            details.getEffectiveName(),
            details.getWritesInProgress(),
            details.getReadsInProgress(),
            details.getPartitionsCached(),
            details.getStatus(),
            details.getStateReason(),
            details.getName(),
            details.getMode(),
            details.getPosition(),
            details.getProgress(),
            details.getLastCheckpoint(),
            details.getEventsProcessedAfterRestart(),
            details.getCheckpointStatus(),
            details.getBufferedEvents(),
            details.getWritePendingEventsBeforeCheckpoint(),
            details.getWritePendingEventsAfterCheckpoint()
        );
    }

    public long getCoreProcessingTime() {
        return coreProcessingTime;
    }

    public long getVersion() {
        return version;
    }

    public long getEpoch() {
        return epoch;
    }

    public String getEffectiveName() {
        return effectiveName;
    }

    public int getWritesInProgress() {
        return writesInProgress;
    }

    public int getReadsInProgress() {
        return readsInProgress;
    }

    public int getPartitionsCached() {
        return partitionsCached;
    }

    public String getStatus() {
        return status;
    }

    public String getStateReason() {
        return stateReason;
    }

    public String getName() {
        return name;
    }

    public String getMode() {
        return mode;
    }

    public String getPosition() {
        return position;
    }

    public float getProgress() {
        return progress;
    }

    public String getLastCheckpoint() {
        return lastCheckpoint;
    }

    public long getEventsProcessedAfterRestart() {
        return eventsProcessedAfterRestart;
    }

    public String getCheckpointStatus() {
        return checkpointStatus;
    }

    public long getBufferedEvents() {
        return bufferedEvents;
    }

    public int getWritePendingEventsBeforeCheckpoint() {
        return writePendingEventsBeforeCheckpoint;
    }

    public int getWritePendingEventsAfterCheckpoint() {
        return writePendingEventsAfterCheckpoint;
    }
}

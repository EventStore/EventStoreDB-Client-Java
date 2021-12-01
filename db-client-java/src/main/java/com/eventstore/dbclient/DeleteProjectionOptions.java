package com.eventstore.dbclient;

public class DeleteProjectionOptions extends OptionsBase<DeleteProjectionOptions> {
    private boolean deleteEmittedStreams;
    private boolean deleteStateStream;
    private boolean deleteCheckpointStream;

    private DeleteProjectionOptions() {
    }

    public static DeleteProjectionOptions get() {
        return new DeleteProjectionOptions();
    }

    public DeleteProjectionOptions deleteEmittedStreams() {
        deleteEmittedStreams = true;
        return this;
    }

    public DeleteProjectionOptions deleteStateStream() {
        deleteStateStream = true;
        return this;
    }

    public DeleteProjectionOptions deleteCheckpointStream() {
        deleteCheckpointStream = true;
        return this;
    }

    public DeleteProjectionOptions deleteEmittedStreams(boolean delete) {
        deleteEmittedStreams = delete;
        return this;
    }

    public DeleteProjectionOptions deleteStateStream(boolean delete) {
        deleteStateStream = delete;
        return this;
    }

    public DeleteProjectionOptions deleteCheckpointStream(boolean delete) {
        deleteCheckpointStream = delete;
        return this;
    }

    public boolean getDeleteEmittedStreams() {
        return deleteEmittedStreams;
    }

    public boolean getDeleteStateStream() {
        return deleteStateStream;
    }

    public boolean getDeleteCheckpointStream() {
        return deleteCheckpointStream;
    }

    public DeleteProjectionOptions keepEmittedStreams() {
        deleteEmittedStreams = false;
        return this;
    }

    public DeleteProjectionOptions keepStateStream() {
        deleteStateStream = false;
        return this;
    }

    public DeleteProjectionOptions keepCheckpointStream() {
        deleteCheckpointStream = false;
        return this;
    }
}

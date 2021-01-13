package com.eventstore.dbclient;

public class DeleteStreamOptions extends OptionsWithExpectedRevisionBase<DeleteStreamOptions> {
    private boolean softDelete;

    private DeleteStreamOptions() {
        this.softDelete = true;
    }

    public static DeleteStreamOptions get() {
        return new DeleteStreamOptions();
    }

    public boolean isSoftDelete() {
        return this.softDelete;
    }

    public DeleteStreamOptions softDelete() {
        this.softDelete = true;
        return this;
    }

    public DeleteStreamOptions hardDelete() {
        this.softDelete = false;
        return this;
    }
}

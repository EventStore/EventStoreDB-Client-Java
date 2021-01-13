package com.eventstore.dbclient;

public class CreateOneTimeProjectionOptions extends OptionsBase<CreateOneTimeProjectionOptions> {
    private CreateOneTimeProjectionOptions() {
    }

    public static CreateOneTimeProjectionOptions get() {
        return new CreateOneTimeProjectionOptions();
    }
}

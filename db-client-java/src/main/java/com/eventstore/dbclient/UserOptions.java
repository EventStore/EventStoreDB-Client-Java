package com.eventstore.dbclient;

public class UserOptions extends OptionsBase<UserOptions> {
    private UserOptions() {
    }

   public static UserOptions get() {
        return new UserOptions();
    }
}

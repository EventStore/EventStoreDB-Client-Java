package com.eventstore.dbclient;

import java.util.function.Consumer;

class Shutdown implements Msg {
    final Consumer<Void> completed;

    public Shutdown(Consumer<Void> completed) {
        this.completed = completed;
    }

   public void complete() {
        completed.accept(null);
   }

    @Override
    public String toString() {
        return "Shutdown";
    }

    @Override
    public void accept(MsgHandler handler) {
        handler.shutdown(this);
    }
}

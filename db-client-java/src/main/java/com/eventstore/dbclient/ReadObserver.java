package com.eventstore.dbclient;

public abstract class ReadObserver<A> {
    public abstract void onNext(ResolvedEvent event);
    public abstract A onCompleted();
    public abstract void onError(Throwable error);
    public void onStreamNotFound() {}
}

package com.eventstore.dbclient;

public class NamedConsumerStrategy {
    public static final String DISPATCH_TO_SINGLE = "DispatchToSingle";
    public static final String ROUND_ROBIN = "RoundRobin";
    public static final String PINNED = "Pinned";

    public static String from(ConsumerStrategy strategy) {
        switch (strategy){
            case DispatchToSingle: return NamedConsumerStrategy.DISPATCH_TO_SINGLE;
            case RoundRobin: return NamedConsumerStrategy.ROUND_ROBIN;
            case Pinned: return NamedConsumerStrategy.PINNED;
        }

        throw new IllegalArgumentException("Unknown ConsumerStrategy specified!");
    }
}

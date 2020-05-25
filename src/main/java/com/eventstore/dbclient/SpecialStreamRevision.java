package com.eventstore.dbclient;

public enum SpecialStreamRevision {
    NO_STREAM,
    STREAM_EXISTS,
    ANY;

    @Override
    public String toString() {
        switch (this) {
            case NO_STREAM:
                return "No Stream";
            case STREAM_EXISTS:
                return "Stream Exists";
            case ANY:
                return "Any Revision";
        }

        throw new IllegalStateException("Bad enumeration value for SpecialStreamRevision");
    }
}

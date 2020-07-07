package com.eventstore.dbclient;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.regex.Pattern;

public class SubscriptionFilterBuilder {
    private int _checkpointIntervalUnsigned = 1;
    private Checkpointer _checkpointer = null;
    private FilterType _filterType = null;
    private Optional<Integer> _maxWindow;
    private RegularFilterExpression _regular = null;
    private PrefixFilterExpression _prefix = null;

    private enum FilterType {
        STREAM,
        EVENT_TYPE;
    }

    public SubscriptionFilterBuilder() {
    }

    public SubscriptionFilterBuilder withMaxWindow(int maxWindow) {
        _maxWindow = Optional.of(maxWindow);
        return this;
    }

    public SubscriptionFilterBuilder withStreamNameRegularExpression(@NotNull String pattern) {
        if (_filterType != null) {
            throw new IllegalStateException(String.format("Filter type is already set to %s", _filterType.name()));
        }

        _filterType = FilterType.STREAM;
        _regular = new RegularFilterExpression(Pattern.compile(pattern));

        return this;
    }

    public SubscriptionFilterBuilder withStreamNamePrefix(@NotNull String prefix) {
        if (_filterType != null) {
            throw new IllegalStateException(String.format("Filter type is already set to %s", _filterType.name()));
        }

        _filterType = FilterType.STREAM;
        _prefix = new PrefixFilterExpression(prefix);

        return this;
    }

    public SubscriptionFilterBuilder withEventTypeRegularExpression(@NotNull String pattern) {
        if (_filterType != null) {
            throw new IllegalStateException(String.format("Filter type is already set to %s", _filterType.name()));
        }

        _filterType = FilterType.EVENT_TYPE;
        _regular = new RegularFilterExpression(Pattern.compile(pattern));

        return this;
    }

    public SubscriptionFilterBuilder withEventTypePrefix(@NotNull String prefix) {
        if (_filterType != null) {
            throw new IllegalStateException(String.format("Filter type is already set to %s", _filterType.name()));
        }

        _filterType = FilterType.EVENT_TYPE;
        _prefix = new PrefixFilterExpression(prefix);

        return this;
    }

    public SubscriptionFilterBuilder withCheckpointer(@NotNull Checkpointer checkpointer, int intervalMultiplierUnsigned) {
        this._checkpointIntervalUnsigned = intervalMultiplierUnsigned;
        this._checkpointer = checkpointer;

        return this;
    }

    public SubscriptionFilterBuilder withCheckpointer(@NotNull Checkpointer checkpointer) {
        return this.withCheckpointer(checkpointer, 1);
    }

    public SubscriptionFilter build() {
        if (_filterType == null) {
            throw new IllegalStateException("No filter type is specified");
        }

        switch (this._filterType) {
            case STREAM:
                StreamFilter s;
                if (_regular != null) {
                    s = new StreamFilter(this._maxWindow, _regular);
                } else if (_prefix != null) {
                    s = new StreamFilter(this._maxWindow, _prefix);
                } else {
                    throw new IllegalStateException("Neither prefix or regular expression stream filter is configured");
                }

                return new SubscriptionFilter(s, this._checkpointIntervalUnsigned, this._checkpointer);

            case EVENT_TYPE:
                EventTypeFilter et;
                if (_regular != null) {
                    et = new EventTypeFilter(this._maxWindow, _regular);
                } else if (_prefix != null) {
                    et = new EventTypeFilter(this._maxWindow, _prefix);
                } else {
                    throw new IllegalStateException("Neither prefix or regular expression event type filter is configured");
                }

                return new SubscriptionFilter(et, this._checkpointIntervalUnsigned, this._checkpointer);

            default:
                throw new IllegalStateException(String.format("Unhandled filter type variant: %s", this._filterType.name()));
        }
    }
}

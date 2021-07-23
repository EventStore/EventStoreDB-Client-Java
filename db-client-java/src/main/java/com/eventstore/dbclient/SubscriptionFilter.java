package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SubscriptionFilter {
    @NotNull
    private final EventFilter filter;

    private final int checkpointIntervalUnsigned;
    private final Checkpointer checkpointer;

    public static SubscriptionFilterBuilder newBuilder() {
        return new SubscriptionFilterBuilder();
    }

    public SubscriptionFilter(@NotNull final EventFilter filter) {
        this.filter = filter;
        this.checkpointer = null;
        this.checkpointIntervalUnsigned = 1; // TODO(jen20): Review this default
    }

    public SubscriptionFilter(@NotNull final EventFilter filter,
                              final int checkpointIntervalUnsigned,
                              @NotNull final Checkpointer checkpointer) {
        this.filter = filter;
        this.checkpointer = checkpointer;
        this.checkpointIntervalUnsigned = checkpointIntervalUnsigned;
    }

    Checkpointer getCheckpointer() {
        return checkpointer;
    }

    /**
     * @deprecated prefer {@link #getBuilder()}
     */
    @Deprecated
    void addToWireReadReq(StreamsOuterClass.ReadReq.Options.Builder optionsBuilder) {
        Shared.FilterOptions.Builder filterOptionsBuilder = this.getBuilder();

        if (filterOptionsBuilder == null) {
            optionsBuilder.setNoFilter(Shared.Empty.getDefaultInstance());
        } else {
            optionsBuilder.setFilter(filterOptionsBuilder);
        }
    }

    Shared.FilterOptions.Builder getBuilder() {
        RegularFilterExpression regex = filter.getRegularFilterExpression();
        PrefixFilterExpression[] prefixes = filter.getPrefixFilterExpressions();
        Optional<Integer> maxSearchWindow = filter.getMaxSearchWindow();

        if (regex != null && prefixes != null && prefixes.length != 0) {
            throw new IllegalArgumentException("Regex and Prefix expressions are mutually exclusive");
        }

        Shared.FilterOptions.Expression expression = null;
        if (regex != null) {
            expression = Shared.FilterOptions.Expression.newBuilder()
                    .setRegex(regex.toString())
                    .build();
        }

        if (prefixes != null && prefixes.length > 0) {
            Shared.FilterOptions.Expression.Builder expressionB = Shared.FilterOptions.Expression.newBuilder();
            Stream.of(prefixes)
                    .map(Object::toString)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(expressionB::addPrefix);
            expression = expressionB.build();
        }

        if (expression == null) {
            return null;
        }

        Shared.FilterOptions.Builder optsB = Shared.FilterOptions.newBuilder();
        if (filter instanceof StreamFilter) {
            optsB.setStreamName(expression);
        }
        if (filter instanceof EventTypeFilter) {
            optsB.setEventType(expression);
        }

        if (maxSearchWindow != null && maxSearchWindow.isPresent()) {
            optsB.setMax(maxSearchWindow.get());
        } else {
            optsB.setCount(Shared.Empty.getDefaultInstance());
        }

        return optsB.setCheckpointIntervalMultiplier(this.checkpointIntervalUnsigned);
    }
}


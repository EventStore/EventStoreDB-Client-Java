package com.eventstore.dbclient;

import java.util.Optional;

public interface EventFilter {
    PrefixFilterExpression[] getPrefixFilterExpressions();

    RegularFilterExpression getRegularFilterExpression();

    Optional<Integer> getMaxSearchWindow();
}

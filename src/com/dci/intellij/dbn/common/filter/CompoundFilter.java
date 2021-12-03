package com.dci.intellij.dbn.common.filter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class CompoundFilter<T> implements Filter<T>{

    @Override
    public final boolean accepts(T object) {
        List<Filter<T>> filters = getFilters();
        for (Filter<T> filter : filters) {
            if (filter != null && !filter.accepts(object)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final int getSignature() {
        return Objects.hash(getFilters());
    }

    @Override
    public boolean isEmpty() {
        return getFilters().isEmpty();
    }

    @NotNull
    public abstract List<Filter<T>> getFilters();
}

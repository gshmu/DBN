package com.dci.intellij.dbn.common.list;

import com.dci.intellij.dbn.common.filter.Filter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class FilteredListBase<T> implements FilteredList<T> {
    protected List<T> base;
    protected Filter<T> filter;


    public FilteredListBase(Filter<T> filter, List<T> base) {
        this.base = base;
        this.filter = filter;
    }

    public FilteredListBase(Filter<T> filter) {
        this.base = new ArrayList<>();
        this.filter = filter;
    }

    @Nullable
    @Override
    public final Filter<T> getFilter() {
        return filter;
    }

    public void setFilter(Filter<T> filter) {
        this.filter = filter;
    }
}

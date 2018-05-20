package com.dci.intellij.dbn.common.list;

import com.dci.intellij.dbn.common.filter.Filter;

import java.util.List;

public class FiltrableListImpl<T> extends AbstractFiltrableList<T> {
    private Filter<T> filter;

    public FiltrableListImpl() {
    }

    public FiltrableListImpl(List<T> list) {
        super(list);
    }

    public FiltrableListImpl(List<T> list, Filter<T> filter) {
        super(list);
        this.filter = filter;
    }

    public FiltrableListImpl(Filter<T> filter) {
        this();
        this.filter = filter;
    }

    public void setFilter(Filter<T> filter) {
        this.filter = filter;
    }


    @Override
    public Filter<T> getFilter() {
        return filter;
    }
}

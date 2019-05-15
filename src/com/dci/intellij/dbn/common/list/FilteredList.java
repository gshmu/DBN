package com.dci.intellij.dbn.common.list;

import com.dci.intellij.dbn.common.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class FilteredList<T> extends ArrayList<T> {
    public FilteredList(List<T> source, Filter<T> filter) {
        for (int i = 0; i < source.size(); i++) {
            T element = source.get(i);
            if (filter.accepts(element)) {
                add(element);
            }
        }
    }
}

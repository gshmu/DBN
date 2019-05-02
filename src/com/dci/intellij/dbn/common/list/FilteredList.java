package com.dci.intellij.dbn.common.list;

import com.dci.intellij.dbn.common.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class FilteredList<T> extends ArrayList<T> {
    public FilteredList(List<T> source, Filter<T> filter) {
        for (T element : source) {
            if (filter.accepts(element)) {
                add(element);
            }
        }
    }
}

package com.dci.intellij.dbn.common.list;

import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.filter.Filter;

public interface FiltrableList<T> extends List<T> {
    List<T> getFullList();

    @Nullable
    Filter<T> getFilter();

    // update methods should not be affected by filtering
    void sort(Comparator<T> comparator);
}

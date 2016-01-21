package com.dci.intellij.dbn.common.list;

import com.dci.intellij.dbn.common.filter.Filter;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public interface FiltrableList<T> extends List<T> {
    List<T> getFullList();

    @Nullable
    Filter<T> getFilter();

    // update methods should not be affected by filtering
    void sort(Comparator<? super T> comparator);
}

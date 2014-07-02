package com.dci.intellij.dbn.data.model.sortable;

import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.sorting.SortingState;

public class SortableDataModelState extends DataModelState {
    protected SortingState sortingState = new SortingState();

    public SortingState getSortingState() {
        return sortingState;
    }

    public void setSortingState(SortingState sortingState) {
        this.sortingState = sortingState;
    }

}

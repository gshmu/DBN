package com.dci.intellij.dbn.data.model.sortable;

import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.sorting.SortingState;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SortableDataModelState extends DataModelState {
    protected SortingState sortingState = new SortingState();
}

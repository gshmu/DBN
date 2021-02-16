package com.dci.intellij.dbn.data.model.sortable;

import com.dci.intellij.dbn.common.list.FilteredList;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridSortingSettings;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SortableDataModel<
        R extends SortableDataModelRow<? extends SortableDataModel<R, C>, C>,
        C extends SortableDataModelCell<R, ? extends SortableDataModel<R, C>>>
        extends BasicDataModel<R, C> {

    private boolean sortingNullsFirst;

    protected SortableDataModel(Project project) {
        super(project);
    }

    public boolean sort(int columnIndex, SortDirection direction, boolean keepExisting) {
        boolean sort = updateSortingState(columnIndex, direction, keepExisting);
        if (sort) {
            sort();
            notifyRowsUpdated(0, getRows().size());
        }
        return sort;
    }

    @NotNull
    @Override
    public SortableDataModelState getState() {
        return (SortableDataModelState) super.getState();
    }

    @Override
    protected DataModelState createState() {
        return new SortableDataModelState();
    }

    private boolean updateSortingState(int columnIndex, SortDirection direction, boolean keepExisting) {
        SortingState sortingState = getSortingState();
        String columnName = getColumnName(columnIndex);
        int maxSortingColumns = getSortingSettings().getMaxSortingColumns();
        return sortingState.applySorting(columnName, direction, keepExisting, maxSortingColumns);
    }

    private DataGridSortingSettings getSortingSettings() {
        DataGridSettings dataGridSettings = DataGridSettings.getInstance(getProject());
        return dataGridSettings.getSortingSettings();
    }


    protected void sortByIndex() {
        Collections.sort(getRows(), INDEX_COMPARATOR);
        updateRowIndexes(0);
    }

    public void sort() {
        sort(getRows());
    }

    protected void sort(List<R> rows) {
        if (rows instanceof FilteredList) {
            FilteredList<R> filteredList = (FilteredList<R>) rows;
            rows = filteredList.getFullList();
        }
        if (getSortingState().isValid()) {
            boolean nullsFirst = DataGridSettings.getInstance(getProject()).getSortingSettings().isNullsFirst();
            this.sortingNullsFirst = nullsFirst;
            Collections.sort(rows);
        }
        updateRowIndexes(rows, 0);
    }

    private static final Comparator<DataModelRow> INDEX_COMPARATOR = new Comparator<DataModelRow>() {
        @Override
        public int compare(DataModelRow row1, DataModelRow row2) {
            return row1.getIndex() - row2.getIndex();
        }
    };


/*
    public SortDirection getSortDirection() {
        return getSortingState().getDirection();
    }
*/

    public SortingState getSortingState() {
        return getState().getSortingState();
    }

    public void setSortingNullsFirst(boolean sortingNullsFirst) {
        this.sortingNullsFirst = sortingNullsFirst;
    }

    public boolean isSortingNullsFirst() {
        return sortingNullsFirst;
    }

/*
    public int getSortColumnIndex() {
        // fixme - cache sort column index somehow
        SortableDataModelState modelState = getState();
        return isDisposed() || modelState == null ? -1 :
                getHeader().getColumnIndex(modelState.getSortingState().getColumnName());
    }
*/
}

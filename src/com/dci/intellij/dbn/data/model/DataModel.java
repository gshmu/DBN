package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.ui.table.DBNTableWithGutterModel;
import com.dci.intellij.dbn.data.find.DataSearchResult;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DataModel<T extends DataModelRow> extends DBNTableWithGutterModel {
    boolean isReadonly();

    Project getProject();

    void setFilter(Filter<T> filter);

    @Nullable
    Filter<T> getFilter();

    @NotNull
    List<T> getRows();

    int indexOfRow(T row);

    @Nullable
    T getRowAtIndex(int index);

    DataModelHeader getHeader();

    @Override
    int getColumnCount();

    ColumnInfo getColumnInfo(int columnIndex);

    @NotNull
    DataModelState getState();

    void setState(DataModelState state);

    DataSearchResult getSearchResult();

    void addDataModelListener(DataModelListener listener);

    void removeDataModelListener(DataModelListener listener);

    boolean hasSearchResult();

    int getColumnIndex(String columnName);
}

package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.data.find.DataSearchResult;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.ListModel;
import java.util.List;

public interface DataModel<T extends DataModelRow> extends DBNTableModel, ListModel {
    boolean isReadonly();

    Project getProject();

    List<T> getRows();

    int indexOfRow(T row);

    T getRowAtIndex(int index);

    DataModelHeader getHeader();

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

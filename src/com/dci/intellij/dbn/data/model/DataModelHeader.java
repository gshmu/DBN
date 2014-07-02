package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.data.type.DBDataType;

import java.util.List;

public interface DataModelHeader extends Disposable {
    List<ColumnInfo> getColumnInfos();

    ColumnInfo getColumnInfo(int columnIndex);

    int getColumnIndex(String name);

    String getColumnName(int columnIndex);

    DBDataType getColumnDataType(int columnIndex);

    int getColumnCount();
}

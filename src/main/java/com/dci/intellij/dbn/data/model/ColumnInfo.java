package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.intellij.openapi.Disposable;

public interface ColumnInfo extends Disposable {
    String getName();
    int getIndex();
    DBDataType getDataType();

    boolean isSortable();
}

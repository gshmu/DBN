package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.common.dispose.UnlistedDisposable;
import com.dci.intellij.dbn.data.type.DBDataType;

public interface ColumnInfo extends UnlistedDisposable {
    String getName();
    int getIndex();
    DBDataType getDataType();

    boolean isSortable();
}

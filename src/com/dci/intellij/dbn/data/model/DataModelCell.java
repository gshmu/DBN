package com.dci.intellij.dbn.data.model;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;
import com.intellij.openapi.Disposable;

public interface DataModelCell extends Disposable, UserValueHolder {
    ColumnInfo getColumnInfo();

    int getIndex();

    @NotNull
    DataModel getModel();

    @NotNull
    DataModelRow getRow();

    boolean isDisposed();
}

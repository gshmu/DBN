package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;
import org.jetbrains.annotations.NotNull;

public interface DataModelCell extends Disposable, UserValueHolder {
    ColumnInfo getColumnInfo();

    int getIndex();

    @NotNull
    DataModel getModel();

    @NotNull
    DataModelRow getRow();
}

package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;
import org.jetbrains.annotations.NotNull;

public interface DataModelCell<
        R extends DataModelRow<M, ? extends DataModelCell>,
        M extends DataModel<R, ? extends DataModelCell>>
        extends Disposable, UserValueHolder {
    ColumnInfo getColumnInfo();

    int getIndex();

    @NotNull
    M getModel();

    @NotNull
    R getRow();
}

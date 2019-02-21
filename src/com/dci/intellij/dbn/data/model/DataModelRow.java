package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.common.dispose.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DataModelRow<T extends DataModelCell> extends Disposable {
    List<T> getCells();

    @Nullable
    T getCell(String columnName);

    @Nullable
    Object getCellValue(String columnName);

    @Nullable
    T getCellAtIndex(int index);

    int getIndex();

    void setIndex(int index);

    @NotNull
    DataModel getModel();
}

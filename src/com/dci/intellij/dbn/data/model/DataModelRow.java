package com.dci.intellij.dbn.data.model;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.Disposable;

public interface DataModelRow<T extends DataModelCell> extends Disposable {
    List<T> getCells();

    T getCell(String columnName);

    Object getCellValue(String columnName);

    T getCellAtIndex(int index);

    int getIndex();

    void setIndex(int index);

    @NotNull
    DataModel getModel();
}

package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.common.dispose.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.language.common.WeakRef;
import lombok.Data;

@Data
public class DataSearchResultMatch {
    private final int startOffset;
    private final int endOffset;
    private final transient WeakRef<DataModelCell> cell;

    public DataSearchResultMatch(DataModelCell cell, int startOffset, int endOffset) {
        this.cell = WeakRef.of(cell);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public DataModelCell getCell() {
        return Failsafe.nn(cell.get());
    }

    public int getColumnIndex() {
        return getCell().getIndex();
    }

    public int getRowIndex() {
        return getCell().getRow().getIndex();
    }
}

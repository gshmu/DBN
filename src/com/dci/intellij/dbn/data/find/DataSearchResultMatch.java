package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.language.common.WeakRef;

public class DataSearchResultMatch {
    private WeakRef<DataModelCell> cell;
    private int startOffset;
    private int endOffset;

    public DataSearchResultMatch(DataModelCell cell, int startOffset, int endOffset) {
        this.cell = WeakRef.from(cell);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public DataModelCell getCell() {
        return cell.get();
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }
}

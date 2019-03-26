package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.common.dispose.Failsafe;
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
        return Failsafe.nn(cell.get());
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public int getColumnIndex() {
        return getCell().getIndex();
    }

    public int getRowIndex() {
        return getCell().getRow().getIndex();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSearchResultMatch)) return false;

        DataSearchResultMatch that = (DataSearchResultMatch) o;

        if (getStartOffset() != that.getStartOffset()) return false;
        if (getEndOffset() != that.getEndOffset()) return false;

        if (getRowIndex() != that.getRowIndex()) return false;
        if (getColumnIndex() != that.getColumnIndex()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getStartOffset();
        result = 31 * result + getEndOffset();
        result = 31 * result + getRowIndex();
        result = 31 * result + getColumnIndex();
        return result;
    }
}

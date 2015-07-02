package com.dci.intellij.dbn.editor.code;

import java.util.List;

import com.intellij.openapi.editor.RangeMarker;

public class SourceCodeOffsets {
    private GuardedBlockMarkers guardedBlocks = new GuardedBlockMarkers();
    int headerEndOffset = 0;

    public void addGuardedBlock(int startOffset, int endOffset) {
        guardedBlocks.addMarker(startOffset, endOffset);
    }

    public GuardedBlockMarkers getGuardedBlocks() {
        return guardedBlocks;
    }

    public int getHeaderEndOffset() {
        return headerEndOffset;
    }

    public void setHeaderEndOffset(int headerEndOffset) {
        this.headerEndOffset = headerEndOffset;
    }

    public void setGuardedBlocks(List<RangeMarker> rangeMarkers) {
        this.guardedBlocks.apply(rangeMarkers);
    }
}

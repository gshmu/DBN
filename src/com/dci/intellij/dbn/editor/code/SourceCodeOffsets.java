package com.dci.intellij.dbn.editor.code;

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
}

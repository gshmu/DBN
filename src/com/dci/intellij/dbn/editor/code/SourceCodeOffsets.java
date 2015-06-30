package com.dci.intellij.dbn.editor.code;

import java.util.HashSet;
import java.util.Set;

import com.intellij.util.Range;

public class SourceCodeOffsets {
    public static final String GUARDED_BLOCK_START_OFFSET_MARKER = "$$DBN_GUARDED_BLOCK_START_OFFSET$$";
    public static final String GUARDED_BLOCK_END_OFFSET_MARKER = "$$DBN_GUARDED_BLOCK_END_OFFSET$$";
    private Set<Range<Integer>> guardedBlocks = new HashSet<Range<Integer>>();
    int headerEndOffset = 0;

    public void addGuardedBlock(int startOffset, int endOffset) {
        guardedBlocks.add(new Range<Integer>(startOffset, endOffset));
    }

    public Set<Range<Integer>> getGuardedBlocks() {
        return guardedBlocks;
    }

    public int getHeaderEndOffset() {
        return headerEndOffset;
    }

    public void setHeaderEndOffset(int headerEndOffset) {
        this.headerEndOffset = headerEndOffset;
    }
}

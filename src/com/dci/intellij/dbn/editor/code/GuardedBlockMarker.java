package com.dci.intellij.dbn.editor.code;

import com.intellij.util.Range;

public class GuardedBlockMarker extends Range<Integer> {
    public static final String START_OFFSET_IDENTIFIER = "$$DBN_GUARDED_BLOCK_START_OFFSET$$";
    public static final String END_OFFSET_IDENTIFIER = "$$DBN_GUARDED_BLOCK_END_OFFSET$$";

    public GuardedBlockMarker(int startOffset, int endOffset) {
        super(startOffset, endOffset);
    }

    public int getStartOffset() {
        return getFrom();
    }

    public int getEndOffset() {
        return getTo();
    }
}

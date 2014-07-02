package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;

public class NestedRangeStartMarker {
    private ParsePathNode parentNode;
    private int offset;

    public NestedRangeStartMarker(ParsePathNode parentNode, int offset) {
        this.parentNode = parentNode;
        this.offset = offset;
    }

    public NestedRangeStartMarker(int offset) {
        this.offset = offset;
    }

    public ParsePathNode getParentNode() {
        return parentNode;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return offset + "";
    }
}

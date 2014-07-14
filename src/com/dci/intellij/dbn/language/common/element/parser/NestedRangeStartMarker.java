package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class NestedRangeStartMarker {
    private ParsePathNode parentNode;
    private PsiBuilder.Marker marker;
    private int offset;

    public NestedRangeStartMarker(ParsePathNode parentNode, PsiBuilder builder, boolean mark) {
        this.parentNode = parentNode;
        this.offset = builder.getCurrentOffset();
        this.marker = mark ? builder.mark() : null;
    }

    public ParsePathNode getParentNode() {
        return parentNode;
    }

    public int getOffset() {
        return offset;
    }

    public void dropMarker() {
        if (marker != null) {
            marker.drop();
        }
    }

    @Override
    public String toString() {
        return offset + " " + (marker != null);
    }
}

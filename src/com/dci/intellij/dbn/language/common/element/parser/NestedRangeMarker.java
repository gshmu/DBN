package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class NestedRangeMarker {
    private ParsePathNode parseNode;
    private PsiBuilder.Marker marker;
    private int offset;

    public NestedRangeMarker(ParsePathNode parseNode, PsiBuilder builder, boolean mark) {
        this.parseNode = parseNode;
        this.offset = builder.getCurrentOffset();
        //this.marker = mark ? builder.mark() : null;
    }

    public ParsePathNode getParseNode() {
        return parseNode;
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

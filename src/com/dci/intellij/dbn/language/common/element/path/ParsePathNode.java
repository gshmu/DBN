package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.intellij.lang.PsiBuilder;

public class ParsePathNode extends BasicPathNode {
    private int startOffset;
    private int currentOffset;
    private PsiBuilder.Marker elementMarker;
    private int depth;

    public ParsePathNode(ElementType elementType, ParsePathNode parent, int startOffset, int position) {
        super(elementType, parent, position);
        this.startOffset = startOffset;
        this.currentOffset = startOffset;
        this.depth = parent == null ? 0 : parent.getDepth() + 1;
    }

    public ParsePathNode getParent() {
        return (ParsePathNode) super.getParent();
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(int currentOffset) {
        this.currentOffset = currentOffset;
    }

    public boolean isRecursive() {
        ParsePathNode parseNode = this.getParent();
        while (parseNode != null) {
            if (parseNode.getElementType() == getElementType() &&
                parseNode.getStartOffset() == getStartOffset()) {
                return true;
            }
            parseNode = parseNode.getParent();
        }
        return false;
    }

    public boolean isRecursive(int currentOffset) {
        ParsePathNode parseNode = this.getParent();
        while (parseNode != null) {
                if (parseNode.getElementType() == getElementType() &&
                        parseNode.getCurrentOffset() == currentOffset) {
                    return true;
                }
            parseNode = parseNode.getParent();
        }
        return false;
    }

    public int incrementIndex(int builderOffset) {
        int index = getCurrentSiblingIndex();
        index++;
        setCurrentSiblingIndex(index);
        setCurrentOffset(builderOffset);
        return index;
    }

    public PsiBuilder.Marker getElementMarker() {
        return elementMarker;
    }

    public void setElementMarker(PsiBuilder.Marker elementMarker) {
        this.elementMarker = elementMarker;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public void detach() {
        super.detach();
        elementMarker = null;
    }

    @Override
    public boolean isSiblingOf(ParsePathNode parentNode) {
        return depth < parentNode.depth && super.isSiblingOf(parentNode);
    }
}


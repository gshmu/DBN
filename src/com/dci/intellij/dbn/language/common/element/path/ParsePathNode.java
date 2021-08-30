package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.intellij.lang.PsiBuilder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParsePathNode extends BasicPathNode<ParsePathNode> {
    private final int startOffset;
    private final int depth;
    private int currentOffset;
    private int cursorPosition;
    private PsiBuilder.Marker elementMarker;

    public ParsePathNode(ElementType elementType, ParsePathNode parent, int startOffset, int cursorPosition) {
        super(elementType, parent);
        this.startOffset = startOffset;
        this.currentOffset = startOffset;
        this.cursorPosition = cursorPosition;
        this.depth = parent == null ? 0 : parent.depth + 1;
    }

    @Override
    public boolean isRecursive() {
        ParsePathNode parseNode = this.parent;
        while (parseNode != null) {
            if (parseNode.elementType == this.elementType &&
                parseNode.startOffset == startOffset) {
                return true;
            }
            parseNode = parseNode.parent;
        }
        return false;
    }

    public boolean isRecursive(int currentOffset) {
        ParsePathNode parseNode = this.parent;
        while (parseNode != null) {
            if (parseNode.elementType == this.elementType &&
                        parseNode.currentOffset == currentOffset) {
                    return true;
                }
            parseNode = parseNode.parent;
        }
        return false;
    }

    public int incrementIndex(int builderOffset) {
        cursorPosition++;
        this.currentOffset = builderOffset;
        return cursorPosition;
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


package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.intellij.lang.PsiBuilder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParsePathNode extends BasicPathNode<ParsePathNode> {
    private final int startOffset;
    private int currentOffset;
    private int cursorPosition;
    private PsiBuilder.Marker elementMarker;

    public ParsePathNode(ElementType elementType, ParsePathNode parent, int startOffset, int cursorPosition) {
        super(elementType, parent);
        this.startOffset = startOffset;
        this.currentOffset = startOffset;
        this.cursorPosition = cursorPosition;
    }

    @Override
    public boolean isRecursive() {
        ParsePathNode parseNode = this.getParent();
        while (parseNode != null) {
            if (parseNode.getElementType() == this.getElementType() &&
                parseNode.startOffset == startOffset) {
                return true;
            }
            parseNode = parseNode.getParent();
        }
        return false;
    }

    public boolean isRecursive(int currentOffset) {
        ParsePathNode parseNode = this.getParent();
        while (parseNode != null) {
            if (parseNode.getElementType() == this.getElementType() &&
                        parseNode.currentOffset == currentOffset) {
                    return true;
                }
            parseNode = parseNode.getParent();
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
}


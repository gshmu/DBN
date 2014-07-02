package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.language.common.element.ElementType;

public class BasicPathNode implements PathNode {
    private int currentSiblingIndex;
    private PathNode parent;
    private ElementType elementType;

    public BasicPathNode(ElementType elementType, PathNode parent, int currentSiblingIndex) {
        this.elementType = elementType;
        this.parent = parent;
        this.currentSiblingIndex = currentSiblingIndex;
    }

    public PathNode getParent() {
        return parent;
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    public int getCurrentSiblingIndex() {
        return currentSiblingIndex;
    }

    public void setCurrentSiblingIndex(int currentSiblingIndex) {
        this.currentSiblingIndex = currentSiblingIndex;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public PathNode getRootPathNode() {
        PathNode pathNode = parent;
        while (pathNode != null) {
            PathNode parentPathNode = pathNode.getParent();
            if (parentPathNode == null) {
                return pathNode;
            }
            pathNode = parentPathNode;
        }
        return this;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public boolean isRecursive() {
        PathNode node = this.getParent();
        while (node != null) {
            if (node.getElementType() == getElementType()) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    public boolean isRecursive(ElementType elementType) {
        if (getElementType() == elementType) {
            return true;
        }
        PathNode node = this.getParent();
        while (node != null) {
            if (node.getElementType() == elementType) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        PathNode parent = this;
        while (parent != null) {
            buffer.insert(0, '/');
            buffer.insert(0, parent.getElementType().getId());
            parent = parent.getParent();
        }
        return buffer.toString();
    }

    public void detach() {
        parent = null;
    }

    public boolean isSiblingOf(ParsePathNode parentNode) {
        PathNode parent = getParent();
        while (parent != null) {
            if (parent == parentNode) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
}

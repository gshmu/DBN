package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.common.list.ReversedList;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;

import java.util.ArrayList;
import java.util.List;

public class BasicPathNode<T extends BasicPathNode> implements PathNode {
    private T parent;
    private final ElementType elementType;

    public BasicPathNode(ElementType elementType, T parent) {
        this.elementType = elementType;
        this.parent = parent;
    }

    @Override
    public T getParent() {
        return parent;
    }

    @Override
    public ElementType getElementType() {
        return elementType;
    }

    @Override
    public PathNode getRootPathNode() {
        BasicPathNode pathNode = parent;
        while (pathNode != null) {
            BasicPathNode parentPathNode = pathNode.parent;
            if (parentPathNode == null) {
                return pathNode;
            }
            pathNode = parentPathNode;
        }
        return this;
    }

    public BasicPathNode getPathNode(ElementTypeAttribute attribute) {
        BasicPathNode pathNode = this;
        while (pathNode != null) {
            if (pathNode.elementType.is(attribute)) {
                return pathNode;
            }
            pathNode = pathNode.parent;
        }
        return null;

    }

    @Override
    public boolean isRecursive() {
        BasicPathNode node = this.parent;
        while (node != null) {
            if (node.elementType == elementType) {
                return true;
            }
            node = node.parent;
        }
        return false;
    }

    @Override
    public boolean isRecursive(ElementType elementType) {
        if (this.elementType == elementType) {
            return true;
        }
        BasicPathNode node = this.parent;
        while (node != null) {
            if (node.elementType == elementType) {
                return true;
            }
            node = node.parent;
        }
        return false;
    }

    @Override
    public int getIndexInParent() {
        return elementType.getIndexInParent(this);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        BasicPathNode parent = this;
        while (parent != null) {
            buffer.insert(0, '/');
            buffer.insert(0, parent.elementType.getId());
            parent = parent.parent;
        }
        return buffer.toString();
    }

    @Override
    public void detach() {
        parent = null;
    }

    public static PathNode buildPathUp(ElementType elementType) {
        List<ElementType> path = new ArrayList<>();
        while (elementType != null) {
            path.add(elementType);
            if (elementType instanceof NamedElementType) break;
            elementType = elementType.getParent();
        }

        BasicPathNode pathNode = null;
        ReversedList<ElementType> reversedPath = ReversedList.get(path);
        for (ElementType pathElement : reversedPath) {
            pathNode = new BasicPathNode(pathElement, pathNode);
        }
        return pathNode;
    }
}

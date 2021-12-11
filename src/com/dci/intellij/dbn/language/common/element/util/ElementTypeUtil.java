package com.dci.intellij.dbn.language.common.element.util;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;

public class ElementTypeUtil {
    public static ElementType getEnclosingElementType(ParsePathNode pathNode, ElementTypeAttribute elementTypeAttribute) {
        ParsePathNode parentNode = pathNode.getParent();
        while (parentNode != null) {
            ElementType elementType = parentNode.getElementType();
            if (elementType.is(elementTypeAttribute)) return elementType;
            parentNode = parentNode.getParent();
        }
        return null;
    }

    public static NamedElementType getEnclosingNamedElementType(ParsePathNode pathNode) {
        ParsePathNode parentNode = pathNode.getParent();
        while (parentNode != null) {
            ElementType elementType = parentNode.getElementType();
            if (elementType instanceof NamedElementType) return (NamedElementType) elementType;
            parentNode = parentNode.getParent();
        }
        return null;
    }
    
}

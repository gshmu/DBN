package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class SequenceElementTypeLookupCache<T extends SequenceElementType> extends AbstractElementTypeLookupCache<T> {

    public SequenceElementTypeLookupCache(T elementType) {
        super(elementType);
    }

    public boolean isFirstPossibleLeaf(LeafElementType leaf, ElementType pathChild) {
        return getElementType().canStartWithElement(pathChild) &&
                pathChild.getLookupCache().canStartWithLeaf(leaf) &&
                !canStartWithLeaf(leaf);
    }

    public boolean isFirstRequiredLeaf(LeafElementType leaf, ElementType pathChild) {
        return getElementType().shouldStartWithElement(pathChild) &&
                pathChild.getLookupCache().shouldStartWithLeaf(leaf) &&
                !shouldStartWithLeaf(leaf);
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        //check only first landmarks within first mandatory element
        ElementTypeRef[] children = getElementType().getChildren();
        for (ElementTypeRef child : children) {
            if (child.getLookupCache().containsLandmarkToken(tokenType, node)) return true;
            if (!child.isOptional()) return false;  // skip if found non optional element
        }
        return false;
    }

    public boolean startsWithIdentifier(PathNode node) {
        ElementTypeRef[] children = getElementType().getChildren();
        for (ElementTypeRef child : children) {
            if (child.getLookupCache().startsWithIdentifier(node)) {
                return true;
            }

            if (!child.isOptional()) {
                return false;
            }
        }
        return false;
    }
}


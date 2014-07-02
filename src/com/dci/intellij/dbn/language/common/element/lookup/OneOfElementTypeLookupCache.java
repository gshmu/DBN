package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class OneOfElementTypeLookupCache extends AbstractElementTypeLookupCache<OneOfElementType> {
    public OneOfElementTypeLookupCache(OneOfElementType elementType) {
        super(elementType);
    }

    public boolean isFirstPossibleLeaf(LeafElementType leaf, ElementType pathChild) {
        return pathChild.getLookupCache().canStartWithLeaf(leaf) && !canStartWithLeaf(leaf);
    }

    public boolean isFirstRequiredLeaf(LeafElementType leaf, ElementType pathChild) {
        return pathChild.getLookupCache().shouldStartWithLeaf(leaf) && !shouldStartWithLeaf(leaf);
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        for (ElementType elementType : getElementType().getPossibleElementTypes()) {
            if (elementType.getLookupCache().containsLandmarkToken(tokenType, node)) return true;
        }
        return false;
    }

    public boolean startsWithIdentifier(PathNode node) {
        for(ElementType elementType : getElementType().getPossibleElementTypes()){
            if (elementType.getLookupCache().startsWithIdentifier(node)) return true;
        }
        return false;
    }
}
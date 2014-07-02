package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class IterationElementTypeLookupCache extends AbstractElementTypeLookupCache<IterationElementType> {
    public IterationElementTypeLookupCache(IterationElementType iterationElementType) {
        super(iterationElementType);
    }

    public boolean isFirstPossibleLeaf(LeafElementType leaf, ElementType pathChild) {
        ElementType iteratedElementType = getElementType().getIteratedElementType();
        return pathChild == iteratedElementType &&
                !canStartWithLeaf(leaf) &&
                iteratedElementType.getLookupCache().canStartWithLeaf(leaf);
    }

    public boolean isFirstRequiredLeaf(LeafElementType leaf, ElementType pathChild) {
        ElementType iteratedElementType = getElementType().getIteratedElementType();
        return pathChild == iteratedElementType &&
                !shouldStartWithLeaf(leaf) &&
                iteratedElementType.getLookupCache().shouldStartWithLeaf(leaf);
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        if (getElementType().getSeparatorTokens() != null) {
            for (TokenElementType separatorToken : getElementType().getSeparatorTokens()) {
                if (separatorToken.getLookupCache().containsLandmarkToken(tokenType, node)) return true;
            }
        }
        return getElementType().getIteratedElementType().getLookupCache().containsLandmarkToken(tokenType, node);
    }

    public boolean startsWithIdentifier(PathNode node) {
        return getElementType().getIteratedElementType().getLookupCache().startsWithIdentifier(node);
    }


}

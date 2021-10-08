package com.dci.intellij.dbn.language.common.element.cache;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;

import java.util.Set;

public class NamedElementTypeLookupCache extends SequenceElementTypeLookupCache<NamedElementType>{

    public NamedElementTypeLookupCache(NamedElementType elementType) {
        super(elementType);
    }

    @Override
    protected void registerLeafInParent(LeafElementType leaf) {
        // walk the tree up for all potential parents
        Set<ElementTypeBase> parents = elementType.getParents();
        if (parents != null) {
            for (ElementTypeBase parentElementType: parents) {
                parentElementType.getLookupCache().registerLeaf(leaf, elementType);
            }
        }
    }

    @Override
    public Set<LeafElementType> captureFirstPossibleLeafs(ElementLookupContext context, Set<LeafElementType> bucket) {
        if (!context.isScanned(elementType)) {
            context.markScanned(elementType);
            return super.captureFirstPossibleLeafs(context, bucket);
        }
        return bucket;
    }

    @Override
    public Set<TokenType> captureFirstPossibleTokens(ElementLookupContext context, Set<TokenType> bucket) {
        if (!context.isScanned(elementType)) {
            context.markScanned(elementType);
            return super.captureFirstPossibleTokens(context, bucket);
        }
        return bucket;
    }
}

package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class OneOfElementTypeLookupCache extends AbstractElementTypeLookupCache<OneOfElementType> {
    public OneOfElementTypeLookupCache(OneOfElementType elementType) {
        super(elementType);
    }

    @Deprecated
    public boolean isFirstPossibleLeaf(LeafElementType leaf, ElementType pathChild) {
        return pathChild.getLookupCache().canStartWithLeaf(leaf) && !canStartWithLeaf(leaf);
    }

    public boolean isFirstRequiredLeaf(LeafElementType leaf, ElementType pathChild) {
        return pathChild.getLookupCache().shouldStartWithLeaf(leaf) && !shouldStartWithLeaf(leaf);
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        for (ElementTypeRef child : getElementType().getChildren()) {
            if (child.getLookupCache().containsLandmarkToken(tokenType, node)) return true;
        }
        return false;
    }

    public boolean startsWithIdentifier(PathNode node) {
        for(ElementTypeRef child : getElementType().getChildren()){
            if (child.getLookupCache().startsWithIdentifier(node)) return true;
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(Set<LeafElementType> bucket, Set<String> parseBranches) {
        boolean branchChecks = getElementType().hasBranchChecks();
        ElementTypeRef[] elementTypeRefs = getElementType().getChildren();
        for (ElementTypeRef elementTypeRef : elementTypeRefs) {
            if (!branchChecks || parseBranches == null || elementTypeRef.supportsBranches(parseBranches)) {
                ElementTypeLookupCache lookupCache = elementTypeRef.getElementType().getLookupCache();
                bucket = lookupCache.collectFirstPossibleLeafs(bucket, parseBranches);
            }
        }
        return bucket;
    }
}
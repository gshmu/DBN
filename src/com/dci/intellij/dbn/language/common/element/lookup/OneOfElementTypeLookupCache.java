package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;

import java.util.Set;

public class OneOfElementTypeLookupCache extends ElementTypeLookupCacheIndexed<OneOfElementType> {
    public OneOfElementTypeLookupCache(OneOfElementType elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstPossibleLeafs.contains(leaf);
        return notInitialized && (isWrapperBeginLeaf(leaf) || source.getLookupCache().couldStartWithLeaf(leaf));
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstRequiredLeafs.contains(leaf);
        return notInitialized && source.getLookupCache().shouldStartWithLeaf(leaf);
    }

    @Override
    public boolean checkStartsWithIdentifier() {
        for(ElementTypeRef child : elementType.getChildren()){
            if (child.getLookupCache().startsWithIdentifier()) return true;
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, Set<LeafElementType> bucket) {
        bucket = super.collectFirstPossibleLeafs(context, bucket);
        ElementTypeRef[] elementTypeRefs = elementType.getChildren();
        for (ElementTypeRef child : elementTypeRefs) {
            if (context.check(child)) {
                ElementTypeLookupCache lookupCache = child.getElementType().getLookupCache();
                bucket = lookupCache.collectFirstPossibleLeafs(context, bucket);
            }
        }
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, Set<TokenType> bucket) {
        bucket = super.collectFirstPossibleTokens(context, bucket);
        ElementTypeRef[] elementTypeRefs = elementType.getChildren();
        for (ElementTypeRef child : elementTypeRefs) {
            if (context.check(child)) {
                ElementTypeLookupCache lookupCache = child.getElementType().getLookupCache();
                bucket = lookupCache.collectFirstPossibleTokens(context, bucket);
            }
        }
        return bucket;
    }
}
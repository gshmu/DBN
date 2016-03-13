package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class LeafElementTypeLookupCache<T extends LeafElementType> extends ElementTypeLookupCacheBase<T>  {
    public LeafElementTypeLookupCache(T elementType) {
        super(elementType);
    }

    @Override
    @Deprecated
    public boolean couldStartWithLeaf(LeafElementType leafElementType) {
        return getElementType() == leafElementType;
    }

    @Override
    public boolean shouldStartWithLeaf(LeafElementType leafElementType) {
        return getElementType() == leafElementType;
    }

    @Override
    public Set<LeafElementType> getFirstPossibleLeafs() {
        HashSet<LeafElementType> leafElementTypes = new HashSet<LeafElementType>(1);
        leafElementTypes.add(getElementType());
        return leafElementTypes;
    }

    @Override
    public Set<LeafElementType> getFirstRequiredLeafs() {
        return getFirstPossibleLeafs();
    }

    @Override
    public Set<TokenType> getFirstRequiredTokens() {
        return getFirstPossibleTokens();
    }

    @Override
    public boolean couldStartWithToken(TokenType tokenType) {
        return getElementType().getTokenType() == tokenType;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        bucket = initBucket(bucket);
        bucket.add(getElementType());
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        bucket = initBucket(bucket);
        collectFirstPossibleTokens(bucket);
        return bucket;
    }

    @Override
    public boolean containsLeaf(LeafElementType elementType) {
        return this.getElementType() == elementType;
    }

    @Override
    public void registerLeaf(LeafElementType leaf, ElementType source) {}
}
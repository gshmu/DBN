package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public abstract class LeafElementTypeLookupCache<T extends LeafElementType> extends ElementTypeLookupCache<T> {
    public LeafElementTypeLookupCache(T elementType) {
        super(elementType);
    }

    @Override
    @Deprecated
    public boolean couldStartWithLeaf(LeafElementType leafElementType) {
        return elementType == leafElementType;
    }

    @Override
    public boolean shouldStartWithLeaf(LeafElementType leafElementType) {
        return elementType == leafElementType;
    }

    @Override
    public Set<LeafElementType> getFirstPossibleLeafs() {
        return Collections.singleton(elementType);
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
        return elementType.getTokenType() == tokenType;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        bucket = initBucket(bucket);
        bucket.add(elementType);
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
        return this.elementType == elementType;
    }

    @Override
    public void registerLeaf(LeafElementType leaf, ElementTypeBase source) {}
}
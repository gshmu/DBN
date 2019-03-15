package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.IterationElementType;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class IterationElementTypeLookupCache extends ElementTypeLookupCache<IterationElementType> {
    public IterationElementTypeLookupCache(IterationElementType elementType) {
        super(elementType);
    }

/*
    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstPossibleLeafs.contains(leaf);
        ElementType iteratedElementType = getElementType().getIteratedElementType();
        return notInitialized && (isWrapperBeginLeaf(leaf) || (source == iteratedElementType && iteratedElementType.getLookupCache().couldStartWithLeaf(leaf)));

    }


    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source) {
        ElementType iteratedElementType = getElementType().getIteratedElementType();
        boolean notInitialized = !firstRequiredLeafs.contains(leaf);
        return notInitialized && source == iteratedElementType && iteratedElementType.getLookupCache().shouldStartWithLeaf(leaf);
    }
*/

    @Override
    public boolean containsToken(TokenType tokenType) {
        return elementType.isSeparator(tokenType) ||
                elementType.isWrappingBegin(tokenType) ||
                elementType.isWrappingEnd(tokenType) ||
                elementType.iteratedElementType.lookupCache.containsToken(tokenType);
    }

    @Override
    public boolean containsLeaf(LeafElementType leafElementType) {
        if (elementType.iteratedElementType.lookupCache.containsLeaf(leafElementType)) {
            return true;
        }

        if (leafElementType instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) leafElementType;
            if (elementType.isSeparator(tokenElementType)) {
                return true;
            }
        }

        return elementType.isWrappingBegin(leafElementType) || elementType.isWrappingEnd(leafElementType);
    }

    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        return  elementType.iteratedElementType.lookupCache.getFirstPossibleTokens();
    }

    @Override
    public Set<TokenType> getFirstRequiredTokens() {
        return elementType.iteratedElementType.lookupCache.getFirstRequiredTokens();
    }

    @Override
    public boolean couldStartWithLeaf(LeafElementType leafElementType) {
        return elementType.isWrappingBegin(leafElementType) || elementType.iteratedElementType.lookupCache.couldStartWithLeaf(leafElementType);
    }

    @Override
    public boolean shouldStartWithLeaf(LeafElementType leafElementType) {
        return elementType.iteratedElementType.lookupCache.shouldStartWithLeaf(leafElementType);
    }


    @Override
    public boolean couldStartWithToken(TokenType tokenType) {
        return elementType.isWrappingBegin(tokenType) ||
                elementType.iteratedElementType.lookupCache.couldStartWithToken(tokenType);
    }

    @Override
    public Set<LeafElementType> getFirstPossibleLeafs() {
        Set<LeafElementType> firstPossibleLeafs = initBucket(null);
        firstPossibleLeafs.addAll(elementType.iteratedElementType.lookupCache.getFirstPossibleLeafs());
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            firstPossibleLeafs.add(wrapping.getBeginElementType());
        }
        return firstPossibleLeafs;
    }

    @Override
    public Set<LeafElementType> getFirstRequiredLeafs() {
        return elementType.iteratedElementType.lookupCache.getFirstRequiredLeafs();
    }

    @Override
    public boolean startsWithIdentifier() {
        return elementType.iteratedElementType.lookupCache.startsWithIdentifier();
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return elementType.iteratedElementType.lookupCache.isFirstPossibleToken(tokenType) || elementType.isWrappingBegin(tokenType);
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return elementType.iteratedElementType.lookupCache.isFirstRequiredToken(tokenType);
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        bucket = super.collectFirstPossibleLeafs(context, bucket);
        return elementType.iteratedElementType.lookupCache.collectFirstPossibleLeafs(context, bucket);
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        bucket = super.collectFirstPossibleTokens(context, bucket);
        return elementType.iteratedElementType.lookupCache.collectFirstPossibleTokens(context, bucket);
    }
}

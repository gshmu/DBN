package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
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
                getIteratedElementLookupCache().containsToken(tokenType);
    }

    @Override
    public boolean containsLeaf(LeafElementType leafElementType) {
        if (getIteratedElementLookupCache().containsLeaf(leafElementType)) {
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
        return getIteratedElementLookupCache().getFirstPossibleTokens();
    }

    @Override
    public Set<TokenType> getFirstRequiredTokens() {
        return getIteratedElementLookupCache().getFirstRequiredTokens();
    }

    @Override
    public boolean couldStartWithLeaf(LeafElementType leafElementType) {
        return elementType.isWrappingBegin(leafElementType) || getIteratedElementLookupCache().couldStartWithLeaf(leafElementType);
    }

    @Override
    public boolean shouldStartWithLeaf(LeafElementType leafElementType) {
        return getIteratedElementLookupCache().shouldStartWithLeaf(leafElementType);
    }


    @Override
    public boolean couldStartWithToken(TokenType tokenType) {
        return elementType.isWrappingBegin(tokenType) ||
                getIteratedElementLookupCache().couldStartWithToken(tokenType);
    }

    @Override
    public Set<LeafElementType> getFirstPossibleLeafs() {
        Set<LeafElementType> firstPossibleLeafs = initBucket(null);
        firstPossibleLeafs.addAll(getIteratedElementLookupCache().getFirstPossibleLeafs());
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            firstPossibleLeafs.add(wrapping.getBeginElementType());
        }
        return firstPossibleLeafs;
    }

    @Override
    public Set<LeafElementType> getFirstRequiredLeafs() {
        return getIteratedElementLookupCache().getFirstRequiredLeafs();
    }

    @Override
    public boolean startsWithIdentifier() {
        return getIteratedElementLookupCache().startsWithIdentifier();
    }

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return getIteratedElementLookupCache().isFirstPossibleToken(tokenType) || elementType.isWrappingBegin(tokenType);
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return getIteratedElementLookupCache().isFirstRequiredToken(tokenType);
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        bucket = super.collectFirstPossibleLeafs(context, bucket);
        return getIteratedElementLookupCache().collectFirstPossibleLeafs(context, bucket);
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        bucket = super.collectFirstPossibleTokens(context, bucket);
        return getIteratedElementLookupCache().collectFirstPossibleTokens(context, bucket);
    }

    private ElementTypeLookupCache getIteratedElementLookupCache() {
        return getIteratedElementType().getLookupCache();
    }

    private ElementTypeBase getIteratedElementType() {
        return elementType.getIteratedElementType();
    }
}

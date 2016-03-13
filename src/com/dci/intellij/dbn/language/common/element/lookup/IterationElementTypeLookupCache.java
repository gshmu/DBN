package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class IterationElementTypeLookupCache extends ElementTypeLookupCacheBase<IterationElementType> {
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
    public void registerLeaf(LeafElementType leaf, ElementType source) {
        ElementType parent = elementType.getParent();
        if (parent != null) {
            parent.getLookupCache().registerLeaf(leaf, elementType);
        }
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return elementType.isSeparator(tokenType) ||
                elementType.isWrappingBegin(tokenType) ||
                elementType.isWrappingEnd(tokenType) ||
                getIteratedElementType().getLookupCache().containsToken(tokenType);
    }

    @Override
    public boolean containsLeaf(LeafElementType leafElementType) {
        ElementType iteratedElementType = getIteratedElementType();
        if (iteratedElementType.getLookupCache().containsLeaf(leafElementType)) {
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
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context) {
        ElementType iteratedElementType = getIteratedElementType();
        return  iteratedElementType.getLookupCache().collectFirstPossibleLeafs(context);
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context) {
        ElementType iteratedElementType = getIteratedElementType();
        return  iteratedElementType.getLookupCache().collectFirstPossibleTokens(context);
    }

    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        return  getIteratedElementType().getLookupCache().getFirstPossibleTokens();
    }

    @Override
    public Set<TokenType> getFirstRequiredTokens() {
        return getIteratedElementType().getLookupCache().getFirstRequiredTokens();
    }

    @Override
    public boolean couldStartWithLeaf(LeafElementType leafElementType) {
        return elementType.isWrappingBegin(leafElementType) || getIteratedElementType().getLookupCache().couldStartWithLeaf(leafElementType);
    }

    @Override
    public boolean shouldStartWithLeaf(LeafElementType leafElementType) {
        return getIteratedElementType().getLookupCache().shouldStartWithLeaf(leafElementType);
    }

    private ElementType getIteratedElementType() {
        return elementType.getIteratedElementType();
    }

    @Override
    public boolean couldStartWithToken(TokenType tokenType) {
        return elementType.isWrappingBegin(tokenType) ||
                getIteratedElementType().getLookupCache().couldStartWithToken(tokenType);
    }

    @Override
    public Set<LeafElementType> getFirstPossibleLeafs() {
        HashSet<LeafElementType> firstPossibleLeafs = new HashSet<LeafElementType>(1);
        firstPossibleLeafs.addAll(getIteratedElementType().getLookupCache().getFirstPossibleLeafs());
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            firstPossibleLeafs.add(wrapping.getBeginElementType());
        }
        return firstPossibleLeafs;
    }

    @Override
    public Set<LeafElementType> getFirstRequiredLeafs() {
        return getIteratedElementType().getLookupCache().getFirstRequiredLeafs();
    }

    @Override
    public boolean startsWithIdentifier() {
        return getIteratedElementType().getLookupCache().startsWithIdentifier();
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return getIteratedElementType().getLookupCache().isFirstPossibleToken(tokenType) || elementType.isWrappingBegin(tokenType);
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return getIteratedElementType().getLookupCache().isFirstRequiredToken(tokenType);
    }

    public boolean startsWithIdentifier(PathNode node) {
        return getElementType().getIteratedElementType().getLookupCache().startsWithIdentifier(node);
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        ElementTypeLookupCache lookupCache = getElementType().getIteratedElementType().getLookupCache();
        return lookupCache.collectFirstPossibleLeafs(context, bucket);
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        ElementTypeLookupCache lookupCache = getElementType().getIteratedElementType().getLookupCache();
        return lookupCache.collectFirstPossibleTokens(context, bucket);
    }
}

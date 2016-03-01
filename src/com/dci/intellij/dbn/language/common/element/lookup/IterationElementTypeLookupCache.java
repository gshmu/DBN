package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class IterationElementTypeLookupCache extends AbstractElementTypeLookupCache<IterationElementType> {
    public IterationElementTypeLookupCache(IterationElementType iterationElementType) {
        super(iterationElementType);
    }

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

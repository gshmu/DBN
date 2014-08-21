package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class WrapperElementTypeLookupCache extends AbstractElementTypeLookupCache<WrapperElementType> {

    public WrapperElementTypeLookupCache(WrapperElementType elementType) {
        super(elementType);
    }

    @Deprecated
    public boolean isFirstPossibleLeaf(LeafElementType leaf, ElementType pathChild) {
        ElementTypeLookupCache startTokenLC = getElementType().getBeginTokenElement().getLookupCache();
        ElementTypeLookupCache wrappedTokenLC = getElementType().getWrappedElement().getLookupCache();
        return startTokenLC.canStartWithLeaf(leaf) ||
               (getElementType().isWrappingOptional() && wrappedTokenLC.canStartWithLeaf(leaf));
    }

    public boolean isFirstRequiredLeaf(LeafElementType leaf, ElementType pathChild) {
        ElementTypeLookupCache startTokenLC = getElementType().getBeginTokenElement().getLookupCache();
        ElementTypeLookupCache wrappedTokenLC = getElementType().getWrappedElement().getLookupCache();

        return (!getElementType().isWrappingOptional() && startTokenLC.shouldStartWithLeaf(leaf)) ||
               (getElementType().isWrappingOptional() && wrappedTokenLC.shouldStartWithLeaf(leaf));
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        return
            getElementType().getBeginTokenElement().getLookupCache().containsLandmarkToken(tokenType) ||
            getElementType().getEndTokenElement().getLookupCache().containsLandmarkToken(tokenType) ||
            getElementType().getWrappedElement().getLookupCache().containsLandmarkToken(tokenType, node);
    }

    public boolean startsWithIdentifier(PathNode node) {
        return getElementType().isWrappingOptional() &&
                getElementType().getWrappedElement().getLookupCache().startsWithIdentifier(node);
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        bucket = initBucket(bucket);
        WrapperElementType elementType = getElementType();
        bucket.add(elementType.getBeginTokenElement());
        if (elementType.isWrappingOptional()) {
            ElementTypeLookupCache lookupCache = elementType.getWrappedElement().getLookupCache();
            lookupCache.collectFirstPossibleLeafs(context, bucket);
        }
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        bucket = initBucket(bucket);
        WrapperElementType elementType = getElementType();
        bucket.add(elementType.getBeginTokenElement().getTokenType());
        if (elementType.isWrappingOptional()) {
            ElementTypeLookupCache lookupCache = elementType.getWrappedElement().getLookupCache();
            lookupCache.collectFirstPossibleTokens(context, bucket);
        }
        return bucket;
    }
}
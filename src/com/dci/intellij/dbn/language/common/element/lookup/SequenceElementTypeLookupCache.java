package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;

import java.util.Set;

public class SequenceElementTypeLookupCache<T extends SequenceElementType> extends ElementTypeLookupCacheIndexed<T> {

    public SequenceElementTypeLookupCache(T elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstPossibleLeafs.contains(leaf);
        return notInitialized && (
                isWrapperBeginLeaf(leaf) ||
                    (couldStartWithElement(source) &&
                     source.getLookupCache().couldStartWithLeaf(leaf)));
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstRequiredLeafs.contains(leaf);
        return notInitialized &&
                shouldStartWithElement(source) &&
                source.getLookupCache().shouldStartWithLeaf(leaf);
    }

    private boolean couldStartWithElement(ElementType elementType) {
        ElementTypeRef[] children = this.elementType.getChildren();
        for (ElementTypeRef child : children) {
            if (child.optional) {
                if (elementType == child.elementType) return true;
            } else {
                return child.elementType == elementType;
            }
        }
        return false;
    }

    private boolean shouldStartWithElement(ElementType elementType) {
        ElementTypeRef[] children = this.elementType.getChildren();
        for (ElementTypeRef child : children) {
            if (!child.optional) {
                return child.elementType == elementType;
            }
        }
        return false;
    }

    @Override
    public boolean checkStartsWithIdentifier() {
        ElementTypeRef[] children = this.elementType.getChildren();
        for (ElementTypeRef child : children) {
            if (child.getLookupCache().startsWithIdentifier()) {
                return true;
            }

            if (!child.optional) {
                return false;
            }
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, Set<LeafElementType> bucket) {
        bucket = super.collectFirstPossibleLeafs(context, bucket);
        bucket = initBucket(bucket);

        ElementTypeRef[] children = elementType.getChildren();
        for (ElementTypeRef child : children) {
            if (context.check(child)) {
                ElementTypeLookupCache lookupCache = child.elementType.getLookupCache();
                lookupCache.collectFirstPossibleLeafs(context, bucket);
            }
            if (!child.optional) break;
        }
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, Set<TokenType> bucket) {
        bucket = super.collectFirstPossibleTokens(context, bucket);
        bucket = initBucket(bucket);

        ElementTypeRef[] children = elementType.getChildren();
        for (ElementTypeRef child : children) {
            if (context.check(child)) {
                ElementTypeLookupCache lookupCache = child.elementType.getLookupCache();
                lookupCache.collectFirstPossibleTokens(context, bucket);
            }
            if (!child.optional) break;
        }
        return bucket;
    }
}


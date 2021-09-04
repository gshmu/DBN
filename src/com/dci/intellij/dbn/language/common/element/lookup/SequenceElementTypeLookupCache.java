package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.SequenceElementType;

import java.util.Set;

public class SequenceElementTypeLookupCache<T extends SequenceElementType> extends ElementTypeLookupCacheIndexed<T> {

    public SequenceElementTypeLookupCache(T elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementTypeBase source) {
        boolean notInitialized = !firstPossibleLeafs.contains(leaf);
        return notInitialized && (
                isWrapperBeginLeaf(leaf) ||
                    (couldStartWithElement(source) &&
                     source.getLookupCache().couldStartWithLeaf(leaf)));
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementTypeBase source) {
        boolean notInitialized = !firstRequiredLeafs.contains(leaf);
        return notInitialized &&
                shouldStartWithElement(source) &&
                source.getLookupCache().shouldStartWithLeaf(leaf);
    }

    private boolean couldStartWithElement(ElementType elementType) {
        ElementTypeRef child = this.elementType.getFirstChild();
        while (child != null) {
            if (child.optional) {
                if (elementType == child.elementType) return true;
            } else {
                return child.elementType == elementType;
            }
            child = child.getNext();
        }
        return false;
    }

    private boolean shouldStartWithElement(ElementType elementType) {
        ElementTypeRef child = this.elementType.getFirstChild();
        while (child != null) {
            if (!child.optional) {
                return child.elementType == elementType;
            }
            child = child.getNext();
        }
        return false;
    }

    @Override
    public boolean checkStartsWithIdentifier() {
        ElementTypeRef child = this.elementType.getFirstChild();
        while (child != null) {
            if (child.getLookupCache().startsWithIdentifier()) {
                return true;
            }

            if (!child.optional) {
                return false;
            }
            child = child.getNext();
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, Set<LeafElementType> bucket) {
        bucket = super.collectFirstPossibleLeafs(context, bucket);
        bucket = initBucket(bucket);

        ElementTypeRef child = this.elementType.getFirstChild();
        while (child != null) {
            if (context.check(child)) {
                child.elementType.getLookupCache().collectFirstPossibleLeafs(context, bucket);
            }
            if (!child.optional) break;
            child = child.getNext();
        }
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, Set<TokenType> bucket) {
        bucket = super.collectFirstPossibleTokens(context, bucket);
        bucket = initBucket(bucket);

        ElementTypeRef child = this.elementType.getFirstChild();
        while (child != null) {
            if (context.check(child)) {
                child.elementType.getLookupCache().collectFirstPossibleTokens(context, bucket);
            }
            if (!child.optional) break;
            child = child.getNext();
        }
        return bucket;
    }
}


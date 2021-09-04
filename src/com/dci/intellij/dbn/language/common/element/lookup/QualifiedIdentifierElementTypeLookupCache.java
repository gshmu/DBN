package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierElementType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class QualifiedIdentifierElementTypeLookupCache extends ElementTypeLookupCacheIndexed<QualifiedIdentifierElementType> {
    public QualifiedIdentifierElementTypeLookupCache(QualifiedIdentifierElementType elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementTypeBase source) {
        for (LeafElementType[] variant : elementType.getVariants()) {
            if (variant[0] == source) return true;
        }
        return false;
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementTypeBase source) {
        for (LeafElementType[] variant : elementType.getVariants()) {
            if (variant[0] == source && !variant[0].isOptional()) return true;
        }
        return false;
    }

    @Override
    public boolean checkStartsWithIdentifier() {
        for (LeafElementType[] elementTypes : elementType.getVariants()) {
            if (elementTypes[0].getLookupCache().startsWithIdentifier()) return true;
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        bucket = initBucket(bucket);
        for (LeafElementType[] elementTypes : elementType.getVariants()) {
            // variants already consider optional leafs
            bucket.add(elementTypes[0]);
        }

        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        bucket = initBucket(bucket);
        for (LeafElementType[] elementTypes : elementType.getVariants()) {
            // variants already consider optional leafs
            bucket.add(elementTypes[0].tokenType);
        }

        return bucket;
    }
}
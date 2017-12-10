package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.BasicElementType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;

public class BasicElementTypeLookupCache extends ElementTypeLookupCacheBaseIndexed<BasicElementType> {
    public BasicElementTypeLookupCache(BasicElementType elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source) {
        return false;
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source) {
        return false;
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return false;
    }

    @Override
    public boolean startsWithIdentifier() {
        return false;
    }

    public boolean checkStartsWithIdentifier() {return false;}

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        return bucket;
    }
}
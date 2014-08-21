package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.BasicElementType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class BasicElementTypeLookupCache extends AbstractElementTypeLookupCache<BasicElementType> {
    public BasicElementTypeLookupCache(BasicElementType elementType) {
        super(elementType);
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return false;
    }

    @Deprecated
    public boolean isFirstPossibleLeaf(LeafElementType leaf, ElementType pathChild) {
        return false;
    }

    public boolean isFirstRequiredLeaf(LeafElementType leaf, ElementType pathChild) {
        return false;
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {return false;}
    
    public boolean startsWithIdentifier(PathNode node) {return false;}

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(@Nullable Set<LeafElementType> bucket, Set<String> parseBranches) {
        return bucket;
    }
}
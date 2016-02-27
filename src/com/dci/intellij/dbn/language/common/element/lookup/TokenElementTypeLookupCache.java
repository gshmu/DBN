package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.intellij.util.containers.HashSet;

import java.util.Set;

public class TokenElementTypeLookupCache extends LeafElementTypeLookupCache<TokenElementType>{
    public TokenElementTypeLookupCache(TokenElementType elementType) {
        super(elementType);
    }

    public void init() {}

    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        HashSet<TokenType> tokenTypes = new HashSet<TokenType>(1);
        tokenTypes.add(getElementType().getTokenType());
        return tokenTypes;
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return getElementType().getTokenType() == tokenType;
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
       return containsToken(tokenType);
    }

    public boolean startsWithIdentifier(PathNode node) {
        return getElementType().getTokenType().isIdentifier();
    }

    @Override
    public boolean startsWithIdentifier() {
        return false;
    }

    @Override
    public boolean containsIdentifiers() {
        return false;
    }
}
package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.intellij.util.containers.HashSet;

import java.util.Set;

public class TokenElementTypeLookupCache extends LeafElementTypeLookupCache<TokenElementType>{
    public TokenElementTypeLookupCache(TokenElementType elementType) {
        super(elementType);
    }

    @Override
    public void init() {}

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return getTokenType() == tokenType;
    }

    protected TokenType getTokenType() {
        return elementType.getTokenType();
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return getTokenType() == tokenType;
    }

    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        HashSet<TokenType> tokenTypes = new HashSet<TokenType>(1);
        tokenTypes.add(getTokenType());
        return tokenTypes;
    }

    @Override
    public void collectFirstPossibleTokens(Set<TokenType> bucket) {
        bucket.add(getTokenType());
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return getTokenType() == tokenType;
    }

    @Override
    public boolean startsWithIdentifier() {
        return false;
    }
}
package com.dci.intellij.dbn.language.common.element.cache;

import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ExecVariableElementType;

import java.util.HashSet;
import java.util.Set;

public class ExecVariableElementTypeLookupCache extends LeafElementTypeLookupCache<ExecVariableElementType>{
    public ExecVariableElementTypeLookupCache(ExecVariableElementType elementType) {
        super(elementType);
    }

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return tokenType.isVariable();
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return tokenType.isVariable();
    }


    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        Set<TokenType> firstPossibleTokens = new HashSet<TokenType>(1);
        SharedTokenTypeBundle sharedTokenTypes = getSharedTokenTypes();
        TokenType variable = sharedTokenTypes.getVariable();
        firstPossibleTokens.add(variable);
        return firstPossibleTokens;
    }

    @Override
    public void captureFirstPossibleTokens(Set<TokenType> bucket) {
        bucket.add(getSharedTokenTypes().getVariable());
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        SharedTokenTypeBundle sharedTokenTypes = getSharedTokenTypes();
        return sharedTokenTypes.getVariable() == tokenType;
    }

    @Override
    public boolean startsWithIdentifier() {
        return false;
    }
}

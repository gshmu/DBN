package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.intellij.util.containers.HashSet;

import java.util.Set;

public class IdentifierElementTypeLookupCache extends LeafElementTypeLookupCache<IdentifierElementType>{
    public IdentifierElementTypeLookupCache(IdentifierElementType elementType) {
        super(elementType);
    }

    public void init() {}

    @Override
    public boolean couldStartWithToken(TokenType tokenType) {
        return tokenType.isIdentifier();
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        SharedTokenTypeBundle sharedTokenTypes = getElementType().getLanguage().getSharedTokenTypes();
        return sharedTokenTypes.getIdentifier() == tokenType || sharedTokenTypes.getQuotedIdentifier() == tokenType;
    }

    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        SharedTokenTypeBundle sharedTokenTypes = getElementType().getLanguage().getSharedTokenTypes();
        TokenType identifier = sharedTokenTypes.getIdentifier();
        TokenType quotedIdentifier = sharedTokenTypes.getQuotedIdentifier();
        HashSet<TokenType> tokenTypes = new HashSet<TokenType>(2);
        tokenTypes.add(identifier);
        tokenTypes.add(quotedIdentifier);
        return tokenTypes;
    }

    public boolean startsWithIdentifier(PathNode node) {
        return true;
    }

    @Override
    public boolean containsIdentifiers() {
        return true;
    }

    @Override
    public boolean startsWithIdentifier() {
        return true;
    }



}
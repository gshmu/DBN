package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class IdentifierElementTypeLookupCache extends LeafElementTypeLookupCache<IdentifierElementType>{
    public IdentifierElementTypeLookupCache(IdentifierElementType elementType) {
        super(elementType);
    }

    public void init() {
        SharedTokenTypeBundle sharedTokenTypes = getElementType().getLanguage().getSharedTokenTypes();
        TokenType identifier = sharedTokenTypes.getIdentifier();
        TokenType quotedIdentifier = sharedTokenTypes.getQuotedIdentifier();
        allPossibleTokens.add(identifier);
        allPossibleTokens.add(quotedIdentifier);
        firstPossibleTokens.add(identifier);
        firstPossibleTokens.add(quotedIdentifier);
        firstRequiredTokens.add(identifier);
        firstRequiredTokens.add(quotedIdentifier);
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        SharedTokenTypeBundle sharedTokenTypes = getElementType().getLanguage().getSharedTokenTypes();
        return sharedTokenTypes.getIdentifier() == tokenType || sharedTokenTypes.getQuotedIdentifier() == tokenType;
    }

    public boolean isFirstPossibleLeaf(LeafElementType leaf, ElementType pathChild) {
        return false;
    }

    public boolean isFirstRequiredLeaf(LeafElementType leaf, ElementType pathChild) {
        return false;
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        return false;
    }

    public boolean startsWithIdentifier(PathNode node) {
        return true;
    }    
}
package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ExecVariableElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

import java.util.HashSet;
import java.util.Set;

public class ExecVariableElementTypeLookupCache extends LeafElementTypeLookupCache<ExecVariableElementType>{
    public ExecVariableElementTypeLookupCache(ExecVariableElementType elementType) {
        super(elementType);
    }

    @Override
    public void init() {}


    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        Set<TokenType> firstPossibleTokens = new HashSet<TokenType>();
        SharedTokenTypeBundle sharedTokenTypes = getElementType().getLanguage().getSharedTokenTypes();
        TokenType variable = sharedTokenTypes.getVariable();
        firstPossibleTokens.add(variable);
        return firstPossibleTokens;
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        SharedTokenTypeBundle sharedTokenTypes = getElementType().getLanguage().getSharedTokenTypes();
        return sharedTokenTypes.getVariable() == tokenType;
    }

    public boolean startsWithIdentifier(PathNode node) {
        return false;
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

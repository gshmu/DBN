package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.object.common.DBObjectType;

import java.util.Set;

public interface ElementTypeLookupCache<T extends ElementType> {
    void registerLeaf(LeafElementType leaf, ElementType pathChild);

    void registerVirtualObject(DBObjectType objectType);

    boolean containsLeaf(LeafElementType leafElementType);

    boolean containsToken(TokenType tokenType);

    boolean containsIdentifier(DBObjectType objectType, IdentifierType identifierType, IdentifierCategory identifierCategory);

    boolean containsIdentifier(DBObjectType objectType, IdentifierType identifierType);

    boolean containsVirtualObject(DBObjectType objectType);

    T getElementType();


    Set<LeafElementType> getFirstPossibleLeafs();

    Set<TokenType> getFirstPossibleTokens();

    boolean canStartWithLeaf(LeafElementType leafElementType);

    boolean shouldStartWithLeaf(LeafElementType leafElementType);

    boolean canStartWithToken(TokenType tokenType);

    boolean shouldStartWithToken(TokenType tokenType);

    Set<LeafElementType> getFirstRequiredLeafs();

    Set<TokenType> getFirstRequiredTokens();

    boolean containsLandmarkToken(TokenType tokenType, PathNode node);
    boolean containsLandmarkToken(TokenType tokenType);

    boolean startsWithIdentifier(PathNode node);    
    boolean startsWithIdentifier();

    boolean containsIdentifiers();

    Set<TokenType> getNextPossibleTokens();

    Set<TokenType> getNextRequiredTokens();

    void init();

    boolean isFirstPossibleLeaf(LeafElementType leaf, ElementType pathChild);

    boolean isFirstRequiredLeaf(LeafElementType leaf, ElementType pathChild);
}

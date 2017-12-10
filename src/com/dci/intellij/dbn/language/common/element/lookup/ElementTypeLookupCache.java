package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;

public interface ElementTypeLookupCache<T extends ElementType> {
    void registerLeaf(LeafElementType leaf, ElementType source);

    boolean containsToken(TokenType tokenType);

    T getElementType();


    boolean containsLeaf(LeafElementType elementType);

    Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context);
    Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket);

    Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context);
    Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket);

    void collectFirstPossibleTokens(Set<TokenType> bucket);

    Set<TokenType> getFirstPossibleTokens();
    Set<TokenType> getFirstRequiredTokens();

    boolean couldStartWithLeaf(LeafElementType leafElementType);
    boolean shouldStartWithLeaf(LeafElementType leafElementType);
    boolean couldStartWithToken(TokenType tokenType);

    Set<LeafElementType> getFirstPossibleLeafs();
    Set<LeafElementType> getFirstRequiredLeafs();

    boolean startsWithIdentifier();

    Set<TokenType> getNextPossibleTokens();

    void init();

    boolean isFirstPossibleToken(TokenType tokenType);

    boolean isFirstRequiredToken(TokenType tokenType);
}

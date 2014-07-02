package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.language.common.TokenType;

import java.util.Set;

public interface SequenceElementType extends ElementType {
    ElementType[] getElementTypes();

    int elementsCount();

    boolean isOptionalFromIndex(int index);

    boolean isLast(int index);

    boolean isFirst(int index);

    boolean isOptional(int index);

    boolean isOptional(ElementType elementType);

    boolean canStartWithElement(ElementType elementType);

    boolean shouldStartWithElement(ElementType elementType);

    boolean isExitIndex(int index);

    boolean containsLandmarkTokenFromIndex(TokenType tokenType, int index);

    Set<TokenType> getFirstPossibleTokensFromIndex(int index);

    boolean isPossibleTokenFromIndex(TokenType tokenType, int index);

    int indexOf(ElementType elementType, int fromIndex);

    int indexOf(ElementType elementType);

    boolean[] getOptionalElementsMap();
}

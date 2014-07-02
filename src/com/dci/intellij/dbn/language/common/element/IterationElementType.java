package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.language.common.TokenType;

public interface IterationElementType extends ElementType {
    ElementType getIteratedElementType();

    TokenElementType[] getSeparatorTokens();

    int[] getElementsCountVariants();

    boolean isSeparator(TokenElementType tokenElementType);

    boolean isSeparator(TokenType tokenType);
}

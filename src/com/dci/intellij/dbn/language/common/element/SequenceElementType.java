package com.dci.intellij.dbn.language.common.element;

import java.util.Set;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;

public interface SequenceElementType extends ElementType {
    ElementTypeRef[] getChildren();

    ElementTypeRef getChild(int index);

    int getChildCount();

    boolean isOptionalFromIndex(int index);

    boolean isLast(int index);

    boolean isFirst(int index);

    boolean canStartWithElement(ElementType elementType);

    boolean shouldStartWithElement(ElementType elementType);

    boolean isExitIndex(int index);

    boolean containsLandmarkTokenFromIndex(TokenType tokenType, int index);

    Set<TokenType> getFirstPossibleTokensFromIndex(int index);

    boolean isPossibleTokenFromIndex(TokenType tokenType, int index);

    int indexOf(ElementType elementType, int fromIndex);

    int indexOf(ElementType elementType);

    boolean hasBranchChecks();

}

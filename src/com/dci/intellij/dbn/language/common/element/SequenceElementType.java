package com.dci.intellij.dbn.language.common.element;

import java.util.Set;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.parser.Branch;

public interface SequenceElementType extends ElementType {
    ElementTypeRef[] getChildren();

    public ElementTypeRef getFirstChild();

    ElementTypeRef getChild(int index);

    int getChildCount();

    boolean isOptionalFromIndex(int index);

    boolean isLast(int index);

    boolean isFirst(int index);

    boolean isExitIndex(int index);

    boolean containsLandmarkTokenFromIndex(TokenType tokenType, int index);

    Set<TokenType> getFirstPossibleTokensFromIndex(ElementLookupContext context, int index);

    boolean isPossibleTokenFromIndex(TokenType tokenType, int index);

    int indexOf(ElementType elementType, int fromIndex);

    int indexOf(ElementType elementType);

    Set<Branch> getCheckedBranches();

}

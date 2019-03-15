package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.language.common.element.impl.IdentifierElementType;
import com.intellij.lang.ASTNode;

public class IdentifierRefPsiElement extends IdentifierPsiElement {
    public IdentifierRefPsiElement(ASTNode astNode, IdentifierElementType elementType) {
        super(astNode, elementType);
    }
}

package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.type.DBObjectType;

import javax.swing.*;

public class IdentifierLookupItemBuilder extends LookupItemBuilder {
    private IdentifierPsiElement identifierPsiElement;
    public IdentifierLookupItemBuilder(IdentifierPsiElement identifierPsiElement) {
        this.identifierPsiElement = identifierPsiElement;
    }

    @Override
    public String getTextHint() {
        IdentifierType identifierType = identifierPsiElement.elementType.getIdentifierType();
        DBObjectType objectType = identifierPsiElement.elementType.getObjectType();
        String objectTypeName = objectType == DBObjectType.ANY ? "object" : objectType.getName();
        String identifierTypeName =
                identifierType == IdentifierType.ALIAS  ? " alias" :
                identifierType == IdentifierType.VARIABLE ? " variable" :
                        "";
        return objectTypeName + identifierTypeName + (identifierPsiElement.isDefinition() ? " def" : " ref");
    }

    @Override
    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        return identifierPsiElement.getChars();
    }

    @Override
    public Icon getIcon() {
        return identifierPsiElement.getObjectType().getIcon();
    }
}
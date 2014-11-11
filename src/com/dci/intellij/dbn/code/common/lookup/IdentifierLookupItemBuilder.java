package com.dci.intellij.dbn.code.common.lookup;

import javax.swing.Icon;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionLookupConsumer;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;

public class IdentifierLookupItemBuilder extends LookupItemBuilder {
    private IdentifierPsiElement identifierPsiElement;
    public IdentifierLookupItemBuilder(IdentifierPsiElement identifierPsiElement) {
        this.identifierPsiElement = identifierPsiElement;
    }

    public String getTextHint() {
        return identifierPsiElement.getElementType().getIdentifierType().name().toLowerCase() + (identifierPsiElement.isDefinition() ? " def" : " ref");
    }

    @Override
    public CodeCompletionLookupItem createLookupItem(Object source, CodeCompletionLookupConsumer consumer) {
        return super.createLookupItem(source, consumer);
    }

    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        return identifierPsiElement.getChars();
    }

    public Icon getIcon() {
        return identifierPsiElement.getObjectType().getIcon();
    }

    @Override
    public void dispose() {
        identifierPsiElement = null;
    }
}
package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;

import javax.swing.*;

public class AliasLookupItemBuilder extends LookupItemBuilder {
    private CharSequence text;
    private boolean isDefinition;

    public AliasLookupItemBuilder(CharSequence text, boolean isDefinition) {
        this.text = text;
        this.isDefinition = isDefinition;
    }

    @Override
    public String getTextHint() {
        return isDefinition ? "alias def" : "alias ref";
    }

    @Override
    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        return text;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
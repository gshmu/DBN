package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;

import javax.swing.*;

public class BasicLookupItemBuilder extends LookupItemBuilder {
    private CharSequence text;
    String hint;
    Icon icon;

    public BasicLookupItemBuilder(CharSequence text, String hint, Icon icon) {
        this.text = text;
        this.hint = hint;
        this.icon = icon;
    }

    public String getTextHint() {
        return hint;
    }

    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        return text;
    }

    public Icon getIcon() {
        return icon;
    }
}
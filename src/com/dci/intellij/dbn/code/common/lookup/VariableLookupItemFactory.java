package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionLookupConsumer;

import javax.swing.Icon;

public class VariableLookupItemFactory extends LookupItemFactory {

    private CharSequence text;
    private boolean isDefinition;

    public VariableLookupItemFactory(CharSequence text, boolean isDefinition) {
        this.text = text;
        this.isDefinition = isDefinition;
    }

    public String getTextHint() {
        return isDefinition ? "variable def" : "variable ref";
    }

    @Override
    public DBLookupItem createLookupItem(Object source, CodeCompletionLookupConsumer consumer) {
        return super.createLookupItem(source, consumer);
    }

    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        return text;
    }

    public Icon getIcon() {
        return null;
    }

    @Override
    public void dispose() {
    }
}
package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionLookupConsumer;
import com.dci.intellij.dbn.code.common.completion.options.sorting.CodeCompletionSortingSettings;

import javax.swing.Icon;

public abstract class LookupItemBuilder {
    public CodeCompletionLookupItem createLookupItem(Object source, CodeCompletionLookupConsumer consumer) {
        CodeCompletionContext context = consumer.getContext();

        CharSequence text = getText(context);
        if (text != null) {
            Icon icon = getIcon();

            String textHint = getTextHint();
            boolean bold = isBold();

            CodeCompletionLookupItem lookupItem;
            CodeCompletionSortingSettings sortingSettings = context.getCodeCompletionSettings().getSortingSettings();
            if (sortingSettings.isEnabled()) {
                int sortingIndex = sortingSettings.getSortingIndexFor(this);
                lookupItem = new CodeCompletionLookupItem(source, icon, text.toString(), textHint, bold, sortingIndex);
            } else {
                lookupItem = new CodeCompletionLookupItem(source, icon, text.toString(), textHint, bold);
            }
            context.getResult().addElement(lookupItem);
            return lookupItem;
        }

        return null;
    }


    public abstract boolean isBold();

    public abstract CharSequence getText(CodeCompletionContext completionContext);

    public abstract String getTextHint();

    public abstract Icon getIcon();
}

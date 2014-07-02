package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.BasicInsertHandler;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.options.sorting.CodeCompletionSortingSettings;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;


public class DBLookupItem extends LookupItem {
    public DBLookupItem(LookupItemFactory lookupItemFactory, @NotNull String text, CodeCompletionContext completionContext) {
        super(lookupItemFactory, NamingUtil.unquote(text));
        setIcon(lookupItemFactory.getIcon());
        if (lookupItemFactory.isBold()) setBold();
        setAttribute(LookupItem.TYPE_TEXT_ATTR, lookupItemFactory.getTextHint());
        setLookupString(text);
        setPresentableText(NamingUtil.unquote(text));
        CodeCompletionSortingSettings sortingSettings = completionContext.getCodeCompletionSettings().getSortingSettings();
        if (sortingSettings.isEnabled()) {
            setPriority(sortingSettings.getSortingIndexFor(lookupItemFactory));
        }
    }

    public  DBLookupItem(Object source, Icon icon, @NotNull String text, String description, boolean bold, double sortPriority) {
        this(source, icon, text, description, bold);
        setPriority(sortPriority);
    }


    public DBLookupItem(Object source, Icon icon, @NotNull String text, String description, boolean bold) {
        super(source, text);
        setIcon(icon);
        if (bold) setBold();
        setAttribute(LookupItem.TYPE_TEXT_ATTR, description);
        setPresentableText(NamingUtil.unquote(text));
        setInsertHandler(BasicInsertHandler.INSTANCE);
    }

    @NotNull
    @Override
    public Object getObject() {
        return super.getObject();
    }

    @Override
    public InsertHandler getInsertHandler() {
        return super.getInsertHandler();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DBLookupItem) {
            DBLookupItem lookupItem = (DBLookupItem) o;
            return lookupItem.getLookupString().equals(getLookupString());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getLookupString().hashCode();
    }
}

package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionLookupConsumer;
import com.dci.intellij.dbn.code.common.completion.options.sorting.CodeCompletionSortingSettings;
import com.dci.intellij.dbn.common.ref.WeakRefCache;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.common.DBObject;

import javax.swing.*;

public abstract class LookupItemBuilder {
    private static final WeakRefCache<DBObject, LookupItemBuilder> sqlCache = WeakRefCache.basic();
    private static final WeakRefCache<DBObject, LookupItemBuilder> psqlCache = WeakRefCache.basic();


    public static LookupItemBuilder of(DBObject object, DBLanguage<?> language) {
        if (language == SQLLanguage.INSTANCE) {
            return sqlCache.get(object, o ->  new ObjectLookupItemBuilder(o.ref(), SQLLanguage.INSTANCE));
        }
        if (language == PSQLLanguage.INSTANCE) {
            return psqlCache.get(object, o -> new ObjectLookupItemBuilder(o.ref(), PSQLLanguage.INSTANCE));
        }

        throw new IllegalArgumentException("Language " + language + " is not supported");
    }


    public void createLookupItem(Object source, CodeCompletionLookupConsumer consumer) {
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
            adjustLookupItem(lookupItem);
            context.getResult().addElement(lookupItem);
        }
    }


    public abstract boolean isBold();

    public abstract CharSequence getText(CodeCompletionContext completionContext);

    protected void adjustLookupItem(CodeCompletionLookupItem lookupItem){}

    public abstract String getTextHint();

    public abstract Icon getIcon();
}

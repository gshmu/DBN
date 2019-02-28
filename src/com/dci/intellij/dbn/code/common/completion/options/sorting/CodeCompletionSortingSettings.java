package com.dci.intellij.dbn.code.common.completion.options.sorting;

import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.completion.options.sorting.ui.CodeCompletionSortingSettingsForm;
import com.dci.intellij.dbn.code.common.lookup.AliasLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.ObjectLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.TokenLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.VariableLookupItemBuilder;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CodeCompletionSortingSettings extends BasicConfiguration<CodeCompletionSettings, CodeCompletionSortingSettingsForm> {
    private boolean enabled = true;
    private List<CodeCompletionSortingItem> sortingItems = new ArrayList<CodeCompletionSortingItem>();

    public CodeCompletionSortingSettings(CodeCompletionSettings parent) {
        super(parent);
    }

    public int getSortingIndexFor(LookupItemBuilder lookupItemBuilder) {
        if (lookupItemBuilder instanceof VariableLookupItemBuilder) {
            return -2;
        }
        if (lookupItemBuilder instanceof AliasLookupItemBuilder) {
            return -1;
        }
        if (lookupItemBuilder instanceof ObjectLookupItemBuilder) {
            ObjectLookupItemBuilder objectLookupItemBuilder = (ObjectLookupItemBuilder) lookupItemBuilder;
            DBObject object = objectLookupItemBuilder.getObject();
            if (Failsafe.check(object)) {
                DBObjectType objectType = object.getObjectType();
                return getSortingIndexFor(objectType);
            }
        }

        if (lookupItemBuilder instanceof TokenLookupItemBuilder) {
            TokenLookupItemBuilder tokenLookupItemBuilder = (TokenLookupItemBuilder) lookupItemBuilder;
            TokenTypeCategory tokenTypeCategory = tokenLookupItemBuilder.getTokenTypeCategory();
            return getSortingIndexFor(tokenTypeCategory);
        }
        return 0;
    }

    public int getSortingIndexFor(DBObjectType objectType) {
        for (int i=0; i<sortingItems.size(); i++) {
            if (sortingItems.get(i).getObjectType() == objectType) {
                return sortingItems.size() - i;
            }
        }
        return 0;
    }

    public int getSortingIndexFor(TokenTypeCategory tokenTypeCategory) {
        for (int i=0; i<sortingItems.size(); i++) {
            if (sortingItems.get(i).getTokenTypeCategory() == tokenTypeCategory) {
                return sortingItems.size() - i;
            }
        }
        return 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }


    public List<CodeCompletionSortingItem> getSortingItems() {
        return sortingItems;
    }

    @Override
    public String getDisplayName() {
        return "Code completion sorting";
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    @NotNull
    public CodeCompletionSortingSettingsForm createConfigurationEditor() {
        return new CodeCompletionSortingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "sorting";
    }

    @Override
    public void readConfiguration(Element element) {
        enabled = getBooleanAttribute(element, "enabled", enabled);
        for (Object child : element.getChildren()) {
            Element childElement = (Element) child;
            CodeCompletionSortingItem sortingItem = new CodeCompletionSortingItem(this);
            sortingItem.readConfiguration(childElement);
            sortingItems.remove(sortingItem);
            sortingItems.add(sortingItem);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        setBooleanAttribute(element, "enabled", enabled);
        for (CodeCompletionSortingItem sortingItem : sortingItems) {
            writeConfiguration(element, sortingItem);
        }
    }

}
